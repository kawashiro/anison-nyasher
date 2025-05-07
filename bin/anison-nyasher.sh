#!/bin/sh

set -e

anti_captcha_key=""
tempmail_so_key=""
rapid_api_key=""
if [ -f "$HOME/.config/anison-nyasher/anti-captcha.key" ]; then
  anti_captcha_key=$(cat "$HOME/.config/anison-nyasher/anti-captcha.key")
fi
if [ -f "$HOME/.config/anison-nyasher/tempmail-so.key" ]; then
  tempmail_so_key=$(cat "$HOME/.config/anison-nyasher/tempmail-so.key")
fi
if [ -f "$HOME/.config/anison-nyasher/rapid-api.key" ]; then
  rapid_api_key=$(cat "$HOME/.config/anison-nyasher/rapid-api.key")
fi

ANTI_CAPTCHA_KEY="$anti_captcha_key" TEMPMAIL_SO_KEY="$tempmail_so_key" RAPID_API_KEY="$rapid_api_key" java -jar /usr/share/java/anison-nyasher.jar "$@"
