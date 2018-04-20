Using a Let's Encrypt (sslforfree.com) Certificate for HTTPS in Spring Boot
========================================================================================================================
sslforfree.com provides 3 files:
- certificate.crt: public SSL certificate
- private.key: private key of the certificate
- ca_bundle.crt: CA certificate validation chain

1) Create the p12 file that can be directly used for the embedded Tomcat in Spring Boot:
$ openssl pkcs12 -export -out gridwars.p12 -inkey private.key -in certificate.crt -certfile ca_bundle.crt
    - Use "changeit" or so as password, must be configured in application.yml.

2) Place the resulting gridwars.p12 file in "web:src/main/resources"

3) Configure embedded Tomcat to use the certificate:
server:
    port: 8443
    ssl:
        enabled: true
        key-store: classpath:gridwars.p12 # Path to the key store that holds the SSL certificate (typically a jks file).
        key-store-password: changeit # Password used to access the key store.
        key-store-type: PKCS12 # Type of the key store.



See also: https://www.namecheap.com/support/knowledgebase/article.aspx/9441/0/tomcat-using-keytool
