package ro.kawashi.aninyasher.remoteservice.anison

import org.apache.logging.log4j.scala.Logging
import ro.kawashi.aninyasher.loginprovider.{LegacyLoginProvider, LoginProvider}
import ro.kawashi.aninyasher.logintransformer.LoginTransformer
import ro.kawashi.aninyasher.logintransformer.strategy._
import ro.kawashi.aninyasher.proxy.{ProxyProvider, TorProxyProvider}
import ro.kawashi.aninyasher.remoteservice.Anison
import ro.kawashi.aninyasher.tor.PosixTorProcess
import ro.kawashi.aninyasher.useragent.{BuiltInUserAgentList, UserAgentList}
import ro.kawashi.aninyasher.util.PasswordGenerator

import scala.annotation.tailrec
import scala.util.{Failure, Random, Success, Try}

object SessionManager {
  def apply(torBinary: String, loginsFilePath: String): SessionManager = {
    new SessionManager(
      BuiltInUserAgentList(),
      TorProxyProvider(new PosixTorProcess(torBinary).start()),
      LegacyLoginProvider(loginsFilePath),
      LoginTransformer()
        .addStrategy(new ExtraSyllableStrategy)
        .addStrategy(new ExtraVowelStrategy)
        .addStrategy(new NumberAddStrategy)
    )
  }
}


class SessionManager(userAgentList: UserAgentList,
                     proxyProvider: ProxyProvider,
                     loginProvider: LoginProvider,
                     loginTransformer: LoginTransformer) extends Logging {

  private val random = new Random()

  def doAnonymously[T](fn: Anison => T): T = {
    fn(Anison(userAgentList.next()))
  }

  def doAuthorized[T](fn: Anison => T): T = {
    val userAgent = userAgentList.next()
    val session = Anison(userAgent, proxyProvider.next())
    if (loginProvider.hasNext) {
      val (login, password) = loginProvider.next()
      session.login(login, password)

      logger.info(s"Logged in as $login using browser $userAgent")
      fn(session)

    } else {
      logger.warn(s"No more logins available, proceeding with registration")
      val login = loginTransformer.transform(getRegistrationSeedLogin(session))
      val password = PasswordGenerator.generate()

      logger.debug(s"Registering as $login :: $password")
      throw new NotImplementedError("Registration is not implemented yet")
    }
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
