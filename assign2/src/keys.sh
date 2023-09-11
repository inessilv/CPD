#!/bin/bash

client_index=$1
client_alias="clientalias$client_index"

echo "Client INDEX: $client_index"
echo "Client Alias: $client_alias"

keytool -genkeypair -alias serveralias -keyalg RSA -keysize 2048 -keystore "$client_index.jks"

keytool -exportcert -alias serveralias -keystore serverkeystore.jks -file server.crt

keytool -genkeypair -alias "$client_alias" -keyalg RSA -keysize 2048 -keystore "${client_index}keystore.jks"
keytool -exportcert -alias "$client_alias" -keystore clientkeystore.jks -file "$client_index.crt"

keytool -import -trustcacerts -alias "$client_alias" -file "$client_index.crt" -keystore serverkeystore.jks
