package ro.kawashi.aninyasher.remoteservice.anison

import scala.annotation.tailrec
import scala.util.{Failure, Random, Success, Try}

import org.apache.logging.log4j.scala.Logging

import ro.kawashi.aninyasher.captcha.{AntiCaptcha, CaptchaSolver}
import ro.kawashi.aninyasher.email.{TemporaryInbox, TempMailSo}
import ro.kawashi.aninyasher.loginprovider.{LegacyLoginProvider, LoginProvider}
import ro.kawashi.aninyasher.logintransformer.LoginTransformer
import ro.kawashi.aninyasher.logintransformer.strategy._
import ro.kawashi.aninyasher.proxy.{ListProxyProvider, ProxyProvider, TorProxyProvider}
import ro.kawashi.aninyasher.remoteservice.Anison
import ro.kawashi.aninyasher.tor.PosixTorProcess
import ro.kawashi.aninyasher.useragent.{BuiltInUserAgentList, UserAgentList}
import ro.kawashi.aninyasher.util.PasswordGenerator

/**
 * Companion object for SessionManager.
 */
object SessionManager {

  private val disableTorFlag = "disable"

  /**
   * Create a new session manager.
   *
   * @param proxy String
   * @param loginsFilePath String
   * @param antiCaptchaKey String
   * @param tempMailSoKey String
   * @param rapidApiKey String
   * @return SessionManager
   */
  def apply(proxy: String, loginsFilePath: String, antiCaptchaKey: String,
            tempMailSoKey: String, rapidApiKey: String): SessionManager = {
    new SessionManager(
      BuiltInUserAgentList(),
      getProxyProvider(proxy),
      LegacyLoginProvider(loginsFilePath),
      LoginTransformer()
        .addStrategy(new ExtraSyllableStrategy)
        .addStrategy(new ExtraVowelStrategy)
        .addStrategy(new NumberAddStrategy),
      AntiCaptcha(antiCaptchaKey),
      TempMailSo(tempMailSoKey, rapidApiKey),
    )
  }

  private def getProxyProvider(proxy: String): ProxyProvider = {
    proxy match {
      case flag: String if flag == disableTorFlag => () => None
      case path: String if path.startsWith("tor:") =>
        TorProxyProvider(new PosixTorProcess(path.replace("tor:", "")).start())
      case filePath: String => ListProxyProvider(filePath)
    }
  }
}

/**
 * Class that prepares a session on anison service and invokes a task provided.
 *
 * @param userAgentList UserAgentList
 * @param proxyProvider ProxyProvider
 * @param loginProvider LoginProvider
 * @param loginTransformer LoginTransformer
 * @param captchaSolver CaptchaSolver
 * @param temporaryInbox TemporaryInbox
 */
class SessionManager(userAgentList: UserAgentList,
                     proxyProvider: ProxyProvider,
                     loginProvider: LoginProvider,
                     loginTransformer: LoginTransformer,
                     captchaSolver: CaptchaSolver,
                     temporaryInbox: TemporaryInbox) extends Logging {

  private val random = new Random()

  /**
   * Do a task anonymously.
   *
   * @param withProxy Boolean
   * @param fn Anison => T
   * @tparam T Type of the result
   * @return T
   */
  def doAnonymously[T](withProxy: Boolean = false)(fn: Anison => T): T = {
    val userAgent = userAgentList.next()
    val proxy = if (withProxy) proxyProvider.next() else None

    fn(Anison(userAgent, proxy))
  }

  /**
   * Do a task authorized. Create a new account if needed.
   *
   * @param fn Anison => T
   * @tparam T Type of the result
   * @return T
   */
  def doAuthorized[T](fn: Anison => T): T = {
    val userAgent = userAgentList.next()
    val session = Anison(userAgent, proxyProvider.next())
    if (loginProvider.hasNext) {
      val (login, password) = loginProvider.next()
      session.login(login, password)

      logger.info(s"Logged in as $login using browser $userAgent")
      fn(session)

    } else {
      logger.warn("No more logins available, proceeding with registration")
      val login = loginTransformer.transform(getRegistrationSeedLogin(session))
      val password = PasswordGenerator.generate()
      val email = temporaryInbox.create(login.toLowerCase)

      logger.info(s"Registering as $login ($email) with password $password")

      val captchaChallenge = session.getRegistrationCaptchaChallenge
      val captchaResult = captchaSolver.solve(captchaChallenge.url, captchaChallenge.key)

      session.register(login, password, email, captchaResult)

      logger.info("Registered successfully! Confirming a email then...")
      val token = temporaryInbox.onNewMail(email => {
        "\\(([0-9a-zA-Z]+)\\)".r.findFirstMatchIn(email.body) match {
          case Some(token) =>
            logger.debug(s"Got email token: ${token.group(1)}")
            Some(token.group(1))
          case None => None
        }
      })

      session.confirmEmail(token)

      session.login(login, password)
      loginProvider += (login, password)

      fn(session)
    }
  }

  /**
   * Create a new session manager with a new login provider to reset logins list.
   *
   * @param newLoginProvider LoginProvider
   * @return SessionManager
   */
  def withNewLoginProvider(newLoginProvider: LoginProvider): SessionManager = {
    new SessionManager(
      userAgentList,
      proxyProvider,
      newLoginProvider,
      loginTransformer,
      captchaSolver,
      temporaryInbox,
    )
  }

  private def getRegistrationSeedLogin(session: Anison): String = {
    val maxId = session.getVoters.max

    @tailrec
    def selectLoop(): String = {
      val id = random.nextInt(maxId)
      Try(session.getUserLogin(id)) match {
        case Success(value) => value
        case Failure(exception) =>
          logger.debug(s"Failed to get user data with id $id: ${exception.getMessage}")
          selectLoop()
      }
    }

    selectLoop()
  }
}
