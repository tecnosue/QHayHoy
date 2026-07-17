### QHayHoy — Menús para no pensar 
Una aplicación móvil orientada a la gestión colaborativa de menús semanales vegetarianos en hogares compartidos. El proyecto nace para resolver un problema cotidiano que afecta a familias, parejas y pisos compartidos: la carga mental que supone decidir cada día qué cocinar, coordinar las preferencias de los miembros y organizar una compra coherente.

La aplicación permite a los miembros de una misma Casa proponer recetas, generar de forma automática el menú de la semana, confirmar su asistencia a cada comida y consultar una lista de la compra que se recalcula dinámicamente en función de los comensales presentes. Toda la información se sincroniza en tiempo real entre los dispositivos de los miembros del hogar, evitando la duplicación de tareas y las decisiones repetidas.

El proyecto se desarrolla con una arquitectura multiplataforma basada en  ``` Kotlin Multiplatform ``` y  ``` Compose Multiplatform ```, lo que permite compartir tanto la lógica de negocio como la interfaz entre Android e iOS. Como backend se utiliza ``` Firebase ```, que aporta autenticación, base de datos en tiempo real y reglas de seguridad declarativas. 

La aplicación se complementa con un catálogo inicial de recetas españolas y la integración con la API externa TheMealDB.


<img width="240" height="467" alt="image" src="https://github.com/user-attachments/assets/f877cba2-570f-4f7c-860a-1cda9368a057" />

Para ver más imagenes de la aplicación y sus funcionalidades:  https://github.com/tecnosue/QHayHoy/blob/main/ANEXO%20III%20%E2%80%94%20Manual%20de%20usuario.pdf






This is a Kotlin Multiplatform project targeting Android.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
