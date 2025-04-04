VEXA
====

Una red social minimalista que permite a los usuarios registrarse, iniciar sesión y compartir publicaciones de solo texto. Su interfaz es sencilla e intuitiva, enfocada en la comunicación sin distracciones.

Ejecutar aplicación
-------------------

Estos son los pasos a seguir para iniciar la aplicación desde cualquier ordenador.

Lo primero, comprobar las dependencias requeridas del archivo pom.xml y la configuración de la base de datos escrita en el archivo *src/main/resources/application.properties*.

Para instalar todas las dependencias y comprobar que compila correctamente usaremos el siguiente comando:

	mvn compile

Debes asegurarte que la base de datos MySQL está bien configurada. Para ello usa el archivo *src/main/resources/dbsetup.sql* para crear la base de datos, un usuario ('spq') con contraseña ('spq') y dar privilegios a ese usuario. Si estás usando la línea de comandos debes ejecutar el siguiente comando:

	mysql –uroot -p < src/main/resources/dbsetup.sql

Ahora, debes inicializar el servidor usando el siguiente comando:

	mvn spring-boot:run

Si todo va bien y no tiene errores, podrás acceder a la aplicación web a través de la url [http://localhost:8080/](http://localhost:8080/). Puedes presionar Ctrl + C para parar la ejecución.

REST API
--------

Esta aplicación utiliza una REST API, la cual está implementada en las clases AuthController y VEXAController. Algunos metodos son los siguientes:
Registrar al usuario.

	POST http://localhost:8080/auth/registration
	Content-Type: application/json
	
	{
		"username": "test",
		"name": "Test",
		"surnames": "Test Test"
		"password": "123",
		"age": 18
	}
	
Iniciar sesión con un usuario.

	POST http://localhost:8080/auth/login
	Content-Type: application/json
	
	{
		"username": "test",
		"password": "123"
	}
	
Logout.
	
	POST http://localhost:8080/auth/logout
	Content-Type: application/json
	
	{
		"token": "4355gdfg5"
	]

Crear un post.

	POST http://localhost:8080/vexa/post
	Content-Type: application/json
	
	{
		"content": "Hello, world!",
		"token": "4355gdfg5"
	]

Obtener todos los posts.

	GET http://localhost:8080/vexa/posts

Empaquetado de aplicación
-------------------------

Para empaquetar la aplicación debes usar el siguiente comando:

	mvn package
	
de esta manera se incluyen todas las librerias de SpringBoot que estén en *target/rest-api-0.0.1-SNAPSHOT.jar*, lo cual se puede distribuir.

Una vez empaquetado, el servidor puede ser ejecutado con:

	java -jar rest-api-0.0.1-SNAPSHOT.jar

Referencias
-----------

* Hemos utilizado inteligencia artificial para ayudarnos con HTML, CSS, Thymeleaf y algo de código Java.
* Se ha utilizado como referencia el proyecto hecho en la asignatura de Diseño del Software.
* Como base para iniciar el proyecto se ha utilizado [esta estructura](https://alud.deusto.es/mod/resource/view.php?id=998978).



