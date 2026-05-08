# upload
* so created an end point for a user to make a reqeust for upload
* the DTO expects two data "name" , and "about"
* and then spring will do the secuirty check and decide if
if the person is worthy of upload
* and then a single UUID for the vid will be returned
## tusd part
now the user will upload to a tusd the files + the users token and 
the id that was returned from spring so that it can match it and do security checks on it
before allowign the upload





# side_quest

leaning up secuirty now here is what is happening
* so there is a massive need for optmizing the Finduserdetails and JWTservice
so first FindUserDetails 

s