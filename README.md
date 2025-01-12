# Formicarium

**Formicarium** este o aplicație de management de proiecte, care permite utilizatorilor să creeze proiecte, să atribuie sarcini, să colaboreze în timp real printr-un sistem de notificări și WebSocket, oferind astfel un flux de lucru modern și interactiv.

## Cuprins

1. [Prezentare Generală](#prezentare-generală)  
2. [Tehnologii Folosite](#tehnologii-folosite)  
3. [Funcționalități Principale](#funcționalități-principale)  
4. [Prioritizare MoSCoW](#prioritizare-moscow)  
5. [Instalare și Configurare](#instalare-și-configurare)  
6. [Cum Se Utilizează](#cum-se-utilizează)  
7. [Capturi de Ecran](#capturi-de-ecran-optional)  
8. [Structura Proiectului](#structura-proiectului)  
9. [Securitate](#securitate)  
10. [Contribuții și Dezvoltare](#contribuții-și-dezvoltare)   
11. [Contact](#contact)  

---

## Prezentare Generală

- Aplicația **Formicarium** permite utilizatorilor să creeze și să administreze proiecte.
- Sarcinile pot fi create și atribuite membrilor echipei, iar statusul lor poate fi actualizat (ToDo, InProgress, Completed etc.).
- Oferă notificări în timp real, cu ajutorul WebSocket și StompJS, pentru a informa rapid utilizatorii despre sarcini noi, modificări, mesaje etc.
- Include un sistem de roluri (Owner, Collaborator, Member) pentru a controla accesul asupra proiectelor și acțiunilor permise.
-Integrează un sistem de securitate bazat pe Spring Security, cu autentificare prin JWT și controale detaliate ale accesului.
-Sarcinile și proiectele sunt organizate pe carduri și liste, oferind o vizualizare clară.

---

## Tehnologii Folosite

- **Java + Spring Boot**  
  - Spring Web, Spring Data JPA, Spring Security, Spring WebSocket, etc.
- **Frontend**  
  - Bootstrap 5  
  - Thymeleaf pentru templating  
  - JavaScript (SockJS, STOMP.js)  
- **Bază de Date**  
  - MySQL
- **Alte**  
  - Lombok
  - JWT pentru autentificare și securizare requesturi  

---

## Funcționalități Principale

1. **Gestionare Proiecte**  
   - Creare/Editare/Ștergere proiecte  
   - Adăugare membri cu roluri diferite (OWNER, COLLABORATOR, MEMBER)  
2. **Gestionare Task-uri**  
   - Creare/Editare/Ștergere task-uri  
   - Atribuirea task-urilor către utilizatori  
   - Statusuri task-uri (TO_DO, IN_PROGRESS, COMPLETED, STOPPED)  
3. **Notificări în Timp Real**  
   - Utilizare WebSocket + StompJS pentru actualizări instant la crearea/actualizarea sarcinilor  
   - Badge cu numărul de notificări necitite  
   - Dropdown cu lista notificărilor (cu butoane mark as read/delete)  
4. **Sistem de Autentificare și Autorizare**  
   - JWT pentru gestionarea sesiunilor  
   - Spring Security + Spring Boot asigură protecția la nivel de endpoint, definind cine are acces la `/login`, `/dashboard`, `/ws/**`, `/tasks`, etc.  
   - Metodele din servicii și controllere conțin validări suplimentare (e.g., rolul OWNER poate edita un proiect, etc.)  
5. **Dashboard Personalizat**  
   - Afișarea sarcinilor recente atribuite utilizatorului  
   - Sinteză a proiectelor active și a statusurilor acestora  
   - Integrare ușoară cu Thymeleaf și template-uri personalizate  

---

## Listă MoSCoW

### **Must Have (Trebuie să aibă)**

1. **Autentificare și Autorizare**  
   - Integrare Java SpringBoot cu Spring Security
   - Implementare JWT pentru gestionarea sesiunilor.  
   - Spring Security pentru protecția endpointurilor.  
   - Roluri de bază în proiecte: OWNER, COLLABORATOR, MEMBER.
   
2. **Gestionare Proiecte**  
   - Creare, Editare, Ștergere, Dezactivare proiecte.
   - Adăugare și eliminare membri din proiect cu atribuirea de roluri.
   
3. **Gestionare Task-uri**  
   - Creare, Editare, Ștergere task-uri.  
   - Atribuirea task-urilor către utilizatori.  
   - Actualizarea statusului task-urilor (ToDo, InProgress, Completed, Stopped).
   
4. **Notificări în Timp Real**  
   - Implementare WebSocket pentru notificări instant. Am implementat WebSockets împreună cu StompJS  
   - Badge cu numărul de notificări necitite.  
   - Dropdown cu lista notificărilor, opțiuni de "Mark as Read" și "Delete" prin butoane separate în dropdown.
   
5. **Dashboard Personalizat**  
   - Afișarea a tuturor sarcini pentru un utilizator
   - Afișarea sarcinilor recente atribuite utilizatorului.  
   - Sinteză a proiectelor active și a statusurilor acestora.

### **Should Have (Ar trebui să aibă)**

1. **Filtrare și Căutare Avansată**  
   - Filtrare task-uri după status, proiect, dată etc.  
   - Căutare task-uri și proiecte prin termeni cheie.
   
2. **Management Avansat al Proiectelor**  
   - Categorii și etichete pentru proiecte.  
   - Atribuire de deadlines și priorități.
   
3. **Sistem de Mesagerie Internă**  
   - Trimiterea și primirea de mesaje între membri. (neimplementat încă)
   - Mesaje în grup pentru un proiect cu toți membri proiectului 
   - Integrarea cu notificările în timp real.
   
4. **Integrare cu Module Externe**  
   - Integrare cu calendare (Google Calendar, Outlook). (neimplementat încă)
   - Export/Import date în diferite formate (CSV, PDF). (neimplementat încă)

### **Could Have (Ar putea avea)**

1. **Analize și Rapoarte**  
   - Generare de rapoarte privind progresul proiectelor.  
   - Vizualizări grafice (grafice de tip bară, linie, etc.) pentru statistici. (neimplementat încă)
   
2. **Notificări Personalizate**  
   - Setări pentru tipurile de notificări primite.  
   - Preferințe privind frecvența notificărilor.
   
3. **Backup și Restaurare**  
   - Funcționalități automate de backup.  
   - Opțiuni de restaurare a datelor în caz de pierdere.

### **Won't Have (Nu va avea)**

1. **Integrare cu Sisteme de Plată**  
   - Procesarea plăților sau gestionarea finanțelor.
   
2. **Funcționalități de E-commerce**  
   - Vânzarea de produse sau servicii direct prin platformă.
   
3. **Gamificare**  
   - Implementarea unor elemente de joc pentru creșterea implicării (badge-uri, leaderboard-uri etc.).

---

## Instalare și Configurare

1. **Clonare Repozitoriu**
   - Clonează proiectul de pe GitHub:
     ```bash
     git clone https://github.com/username/formicarium.git
     cd formicarium
     ```

2. **Configurare Bază de Date**
   - Deschide fișierul `application.properties` (sau `application.yml`) și configurează datele de conexiune la baza de date:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/formicarium
     spring.datasource.username=utilizator
     spring.datasource.password=parola
     spring.jpa.hibernate.ddl-auto=update
     ```

3. **Construire Proiect**
   - Rulează comenzile pentru a compila și construi aplicația:
     ```bash
     mvn clean install
     ```

4. **Rulare Aplicație**
   - Poți rula aplicația utilizând Maven:
     ```bash
     mvn spring-boot:run
     ```
   - Sau folosind fișierul `.jar` generat:
     ```bash
     java -jar target/formicarium-0.0.1-SNAPSHOT.jar
     ```

5. **Accesare Aplicație**
   - După ce serverul pornește, accesează aplicația în browser:
     ```
     http://localhost:8080/
    
     ```

   - Endpoint-ul `/` contine pagina de prezentare (pagina de landing)
   - Pagina de autentificare este disponibilă la `/login`, iar pentru înregistrare (dacă este activată), la `/register`.

## Cum Se Utilizează

1. **Autentificare / Înregistrare**
   - Dacă opțiunea este activată, utilizatorii pot crea un cont accesând `/register`.
   - După înregistrare, utilizatori se autentifică prin pagina `/login`.
   - După autentificare, utilizatorii vor fi direcționați către `/dashboard`.

2. **Dashboard**
   - Pe dashboard, utilizatorii pot vedea sarcinile recente, proiectele active și alte informații personalizate.

3. **Gestionare Proiecte**
   - Accesează secțiunea **Projects** pentru a crea un proiect nou.
   - Completează numele, descrierea și detaliile proiectului.
   - Adaugă membri la proiect și alocă-le roluri (OWNER, COLLABORATOR, MEMBER).

4. **Gestionare Task-uri**
   - În secțiunea **Tasks**, utilizatorii pot crea sarcini pentru un proiect specific.
   - Sarcinile pot fi asignate altor utilizatori și au diverse statusuri (ToDo, InProgress, Completed, etc.).

5. **Notificări**
   - Clopotul din colțul dreapta sus arată notificările în timp real.
   - Fiecare notificare poate fi marcată ca "Read" sau ștearsă.

6. **Securitate și Roluri**
   - Fiecare acțiune este permisă doar în funcție de rolul utilizatorului:
     - **OWNER**: Poate crea/editare/șterge proiecte și sarcini.
     - **COLLABORATOR**: Poate crea și actualiza sarcini.
     - **MEMBER**: Poate doar vizualiza sarcini și proiecte.

## Structura Proiectului

    src
       └───main
│       ├───java
│       │   └───com
│       │       └───example
│       │           └───formicarium
│       │               ├───config
│       │               ├───controller
│       │               ├───dto
│       │               ├───entity
│       │               ├───filter
│       │               ├───repository
│       │               └───service
│       └───resources
│           ├───static
│           │   ├───css
│           │   └───images
│           └───templates
│               └───fragments
└───target
    ├───classes
    │   ├───com
    │   │   └───example
    │   │       └───formicarium
    │   │           ├───config
    │   │           ├───controller
    │   │           ├───dto
    │   │           ├───entity
    │   │           ├───filter
    │   │           ├───repository
    │   │           └───service
    │   ├───static
    │   │   ├───css
    │   │   └───images
    │   └───templates
    │       └───fragments
    ├───generated-sources
    │   └───annotations
    ├───maven-archiver
    └───maven-status
        └───maven-compiler-plugin
            └───compile
                └───default-compile

## Securitate

1. **Autentificare JWT**
   - Aplicația folosește JSON Web Tokens (JWT) pentru autentificare și gestionarea sesiunilor utilizatorilor.
   - Token-ul este generat la autentificare și transmis cu fiecare request protejat.

2. **Spring Security**
   - Accesul la endpoint-uri este controlat prin configurări în `SecurityConfig.java`.
   - Exemplu: Numai utilizatorii autentificați pot accesa `/dashboard`, `/tasks`, `/projects`.

3. **Roluri**
   - **OWNER**: Poate crea, edita și șterge proiecte și sarcini.
   - **COLLABORATOR**: Poate gestiona sarcini în proiectele asignate.
   - **MEMBER**: Are permisiuni de vizualizare.

4. **Endpoint-uri Protejate**
   - WebSocket (`/ws`) este protejat, permițând doar utilizatorilor autentificați să se aboneze la notificări.
   - Sarcinile și proiectele au verificări de acces pe bază de rol.

5. **Prevenirea Atacurilor**
   - Toate formularele sunt protejate împotriva atacurilor CSRF (Cross-Site Request Forgery).
   - Toate datele sunt validate pe server înainte de a fi procesate.

## Contact

Pentru întrebări, sugestii sau probleme, poți să mă contactezi la:
- **Email**: [axintepatrick@gmail.com](mailto:axintepatrick@gmail.com)


Dezvoltat de **Patrick Axinte**. 😊
