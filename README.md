# Chat zadatak iz agentskih tehnologija

1) Importovati JAR2020, JAR2020Client, WAR2020 i EAR2020 u Eclipse IDE

2) Otici u JAR2020 -> src -> beans -> HostManager i promeniti:
    u PATH promenljivoj IP adresu u IP adresu Vaseg master racunara
    u MASTERIP promenljivoj IP adresu u IP adresu Vaseg master racunara
    
3) Pokrenuti server desnim klikom na EAR2020 -> Run as -> Run on Server
   Odabrati radio button opciju "Manually define a new server", WildFly11 i u polje "Server's host name" uneti IP adresu Vaseg raunara
   Neophodno je prvo pokrenuti master cvor, a tek onda ostale cvorove da bi handshake bio uspesan

4) Otici u ControlPanel -> System and Security -> Windows Defender Firewall -> Turn Windows Defender Firewall on or off i iskljuciti     Firewall na privatnim i javnim mrezama (ovo nije neophodno ukoliko ste dozvolili Eclipse-u da komunicira kroz Firewall) 

5) Otici na http://{IP ADRESA VASEG RACUNARA}:8080/WAR2020/
