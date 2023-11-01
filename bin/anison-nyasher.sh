#!/bin/sh

set -e

anti_captcha_key=""
if [ -f "$HOME/.config/anison-nyasher/anti-captcha.key" ]; then
  anti_captcha_key=$(cat "$HOME/.config/anison-nyasher/anti-captcha.key")
fi

ANTI_CAPTCHA_KEY="$anti_captcha_key" java -jar /usr/share/java/anison-nyasher.jar "$@"
