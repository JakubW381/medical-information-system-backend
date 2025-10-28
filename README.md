# medical-information-system-backend

create .env with params

    ADMIN_EMAIL= medical system admin
    ADMIN_PASSWORD= medical system admin password
    API_KEY= cloudinary api key
    API_SECRET= cloudinary secret
    APP_PASS= smtp (gmail) app password
    APP_USERNAME= smtp (gmail) username
    CLOUD_NAME= cloudinary cloud name
    DB_NAME= desired postgre db name
    DB_PASSWORD= desired postgre db password
    DB_USERNAME= desired postgre db username
    SECRET_SHA= jwt secret hash


create a jar file 

    ./mvnw clean package -DskipTests

build the container

    docker-compose build

shutdown and clear containers volumes

    docker-compose up

start the container

    docker-compose up

shutdown the container

    docker-compose down

## Endpoints

### AuthController /api/auth
1. /login (POST)
   - Purpose: Signing into user account, gives httpOnly Cookie with JWT token as a response.
      ```json
         {
           "email" : 
           "password" : 
         }
      ```
     
2. /logout (POST)
    - Purpose:  Logout and clear the cookie
       

3. /role (GET)
#### Calling this endpoint is only possible with Auth Bearer Token with JWT as a cookie or header
    - Purpose: Currently logged user role

### AdminController /api/admin
#### Calling this controller is only possible with Auth Bearer Token with JWT as a cookie or header with an Admin Role
1. /register-doctor (POST)
    - Purpose: Signing up a new user with Doctor Profile
       ```json
          {
            "name" : 
            "lastName" : 
            "email" : 
            "pesel" : 
            "role" : 
            "specialization" :
            "department" : 
            "position" : 
            "professionalLicenseNumber" : 
          }
       ```
2. /user-doctor (POST)
   - Purpose: Attaching a doctor profile to existing user
      ```json
       {
         "name" : 
         "lastName" : 
   
         "specialization" : 
         "department" : 
         "position" : 
         "professionalLicenseNumber" : 
       }
      ```

3. /user-patient (POST)
   - Purpose: Attaching a patient profile to existing user
       ```json
       {
         "name" : 
         "lastName" : 
         "email" : 
         "pesel" : 
         "role" : 
         "gender" : 
         "address" : 
         "phoneNumber" : 
         "bloodType" : 
         "allergies" : 
         "chronicDiseases" : 
         "medications" : 
         "insuranceNumber" : 
       }
       ```
### UserController (Patient) /api/user
#### Calling this controller is only possible with Auth Bearer Token with JWT as a cookie or header 
1. /update-patient (POST)
    - Purpose: Patient can update their own info
        ```json
        {
          "name" : 
          "lastName" : 
          "pesel" : 
          "role", : 
          "dateOfBirth" : 
          "gender" : 
          "address" : 
          "phoneNumber" : 
          "bloodType" : 
          "allergies" : 
          "chronicDiseases" : 
          "medications" : 
          "insuranceNumber" : 
        }
        ```
2. /patient (GET)
    - Purpose: Get patient Info
    - Response : 
        ```json
        {
          "patientId" : 
          "name" : 
          "lastName" : 
          "pesel" :
          "dateOfBirth" : 
          "gender" : 
          "address" : 
          "phoneNumber" : 
          "bloodType" : 
          "allergies" : 
          "chronicDiseases" : 
          "medications" : 
          "insuranceNumber" : 
        }
        ```

3. /pass-change (POST)
    - Purpose: Lets user changing their own password
    ```json
        {
          "newPass" : 
        }
    ```
   - Response
   
4.  /gallery (POST)
    - Purpose: Paginated document gallery
        ```json
        {
          "page" : 
          "year" : 
          "search" : 
        }
        ```
    - Response: Page of document records with thumbnails
      ```json
        {
          "items" : [
                      {
                        "id" : 
                        "patient" : 
                        "sender" : 
                        "dateTime" : 
                        "tags" : [String, String, ...] 
                        "thumbnailSignedURL" :                  
                      },{...},{...}    
                    ],
          "size" : 
          "current" :
          "totalElements" : 
          "totalPages" :  
        }
        ```
5. /document/{id} (GET) 
    - Purpose: Get document by ID
    - Response: Document in Base64 format (String)


6. /document/{id}/share (GET)
    - Purpose: Sharing document outside the system for 24h
    - Response: Temporary URL (String)

7. /upload (POST)
    - Purpose: Uploading files[] to users profile
      @RequestParam("files") MultipartFile[] files
    - Response: "Files processed" or Exception

### PublicController /api/public
    
1. /document/{tokenString} (GET)
    - Purpose: Using temporary link to access shared file
    - Response: shared file in base64 format


### LabController
# #TODO
