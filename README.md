# GurryWallet

Una aplicación Android especializada en la generación de pruebas criptográficas zero-knowledge (ZK) para credenciales digitales. GurryWallet actúa como un intermediario seguro que permite a aplicaciones terceras (como Gurry) obtener pruebas verificables de atributos de credenciales sin exponer los datos sensibles originales.

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Arquitectura](#arquitectura)
- [Flujo de Funcionamiento](#flujo-de-funcionamiento)
- [Componentes Principales](#componentes-principales)
- [Generación de Pruebas ZK](#generación-de-pruebas-zk)
- [Integración con Gurry](#integración-con-gurry)
- [Construcción y Ejecución](#construcción-y-ejecución)

---

## Descripción General

### ¿Qué es GurryWallet?

GurryWallet es una **billetera digital de credenciales** que implementa un sistema de **pruebas criptográficas zero-knowledge (ZK)** basado en la especificación **ISO 18013-5** (estándar para licencias de conducir digitales y documentos de identidad). Permite que los usuarios prueben ciertas características de sus credenciales sin revelar información personal innecesaria.

### Características Principales

- ✅ **Gestión de Credenciales Múltiples**: DNI, Carnet de conducir, Tarjeta Sanitaria, Carnet de Profesional, Carnet Joven
- ✅ **Pruebas Zero-Knowledge**: Genera pruebas criptográficas que verifican afirmaciones sin exponer datos
- ✅ **Integración Intent**: Comunica pruebas generadas a aplicaciones cliente como Gurry
- ✅ **Base de Datos Local**: Almacena credenciales de usuarios en SQLite
- ✅ **Interfaz Compose**: UI moderna en Jetpack Compose
- ✅ **Nativo JNI**: Código C++ optimizado para cálculos criptográficos complejos

---

## Arquitectura

### Componentes del Sistema

```
┌─────────────────────────────────────────────────────────┐
│                    APLICACIÓN GURRY                     │
│         (App cliente que solicita pruebas)              │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ Intent Request
                     │ (Sin datos sensibles)
                     ▼
┌─────────────────────────────────────────────────────────┐
│              GURRYWALLET (Tu Aplicación)                │
│  ┌──────────────────────────────────────────────────┐  │
│  │            Capa de Aplicación (Kotlin)          │  │
│  │  • MainActivity                                  │  │
│  │  • WalletScreen / SettingsScreen                │  │
│  │  • WalletViewModel (Lógica de negocio)          │  │
│  └──────┬───────────────────────────────────────┬──┘  │
│         │                                       │      │
│  ┌──────▼─────────────────────────────────────┬─▼──┐  │
│  │      Capa de Persistencia (Room DB)        │    │  │
│  │  • CredentialEntity (Credenciales)         │    │  │
│  │  • UserEntity (Usuarios)                   │    │  │
│  │  • WalletDao (Acceso a datos)              │    │  │
│  └──────┬────────────────────────────────────────┬──┘  │
│         │                                        │      │
│         │ JNI Call                              │      │
│         │ executeProveZkNative(credIndex)       │      │
│         ▼                                        ▼      │
│  ┌──────────────────────────────────────────────────┐  │
│  │     Librería Nativa (C++ via JNI)              │  │
│  │  • gurry_native.so (Binario compilado)         │  │
│  │  • Interfaz a Longfellow-ZK                    │  │
│  └──────────────────────────────────────────────────┘  │
│         │                                               │
│         │ Utiliza                                       │
│         ▼                                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │    Motor Criptográfico (Longfellow-ZK C++)     │  │
│  │  • Prover: Genera pruebas ZK                    │  │
│  │  • Circuitos: Lógica de verificación            │  │
│  │  • Transcripts: Sesiones criptográficas         │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                     │
                     │ Intent Response
                     │ (ZK Proof + Transcript)
                     ▼
┌─────────────────────────────────────────────────────────┐
│              SERVIDOR DE VERIFICACIÓN GURRY             │
│         (Verifica la prueba usando run_mdoc_verifier)   │
└─────────────────────────────────────────────────────────┘
```

### Stack Tecnológico

| Componente | Tecnología | Propósito |
|-----------|-----------|----------|
| **Lenguaje UI** | Kotlin + Jetpack Compose | Interfaz moderna de usuario |
| **Persistencia** | Room (SQLite) | Almacenamiento de credenciales |
| **Arquitectura** | MVVM + Repository Pattern | Separación de responsabilidades |
| **Async** | Kotlin Coroutines | Operaciones no-bloqueantes |
| **Criptografía** | Longfellow-ZK (C++) | Motor de pruebas ZK |
| **JNI** | Java Native Interface | Puente Java-C++ |
| **Build** | Gradle + CMake | Compilación híbrida |

---

## Flujo de Funcionamiento

### 1. Inicio de la Aplicación

```
User lanza GurryWallet
    ▼
MainActivity.onCreate()
    ▼
Se cargan credenciales del usuario actual desde Room
    ▼
WalletScreen muestra tarjetas de credenciales
```

### 2. Solicitud de Prueba (Flujo Completo)

```
┌─────────────────────────────────────────────────────────────┐
│ PASO 1: Aplicación Gurry solicita una prueba               │
└─────────────────────────────────────────────────────────────┘

Gurry abre GurryWallet via Intent con:
  • Credencial solicitada
  • Atributos a probar (ej: "es_mayor_de_edad")
  • Challenge/Session ID

┌─────────────────────────────────────────────────────────────┐
│ PASO 2: Usuario selecciona credencial en GurryWallet       │
└─────────────────────────────────────────────────────────────┘

onCardClick() en CredentialCard
    │
    └─→ walletViewModel.generarPruebaEdad(indice)
            │
            └─→ WalletViewModel.generarPruebaEdad()
                    │
                    └─→ Dispatcher.IO (Corrutine - Fondo)
                        │
                        └─→ ejecutarPruebaZkNativa(indice, cacheDir)

┌─────────────────────────────────────────────────────────────┐
│ PASO 3: Llamada a código nativo (JNI)                      │
└─────────────────────────────────────────────────────────────┘

executeProveZkNative(credIndex, cacheDir)
    │
    └─→ [Salta a librería C++: gurry_native.so]
        │
        ├─→ Carga credencial desde Room
        ├─→ Obtiene los datos mDL (ISO 18013-5)
        ├─→ Lee el Transcript de sesión
        └─→ Llama a run_mdoc_prover()

┌─────────────────────────────────────────────────────────────┐
│ PASO 4: Generación de la Prueba ZK                          │
└─────────────────────────────────────────────────────────────┘

run_mdoc_prover(
    circuit_bytes,              // Circuito ZK comprimido
    mdoc,                       // Credencial ISO 18013-5
    issuer_public_key,          // Clave del emisor
    transcript,                 // Sesión (session ID + desafío)
    requested_attributes,       // Atributos a probar
    current_time,               // Timestamp formato ISO
    zk_spec_version             // Versión del sistema ZK
)
    │
    ├─→ Valida la credencial contra clave del emisor
    ├─→ Verifica que los atributos existen
    ├─→ Compila el circuito con los datos
    ├─→ Ejecuta el prover Longfellow-ZK
    │   (Genera commitments, sumcheck, Ligero proof)
    └─→ Retorna: proof_bytes (prueba serializada)

┌─────────────────────────────────────────────────────────────┐
│ PASO 5: Retorno de la Prueba a la App                       │
└─────────────────────────────────────────────────────────────┘

JNI retorna: Array<ByteArray>[proof, transcript]
    │
    └─→ Kotlin: Pair(proof, transcript)
        │
        └─→ MainActivity.onProofReady(resultPair)
            │
            ├─→ Crea Intent Response
            ├─→ Adjunta: putExtra("zk_proof", proof)
            ├─→ Adjunta: putExtra("zk_transcript", transcript)
            └─→ setResult(Activity.RESULT_OK, resultIntent)
                │
                └─→ GurryWallet cierra y devuelve a Gurry

┌─────────────────────────────────────────────────────────────┐
│ PASO 6: Verificación (nota importante)                      │
└─────────────────────────────────────────────────────────────┘

En esta implementación la verificación no se realiza en un servidor remoto, sino en la propia aplicación cliente (por ejemplo, la app `Gurry`). Es decir, Gurry recibe `proof` + `transcript` desde `GurryWallet` y la verificación puede llevarse a cabo dentro de la app cliente llamando a las rutinas de verificación (por ejemplo `run_mdoc_verifier`) localmente.

Nota adicional: los "transcripts" usados para pruebas en este repositorio no se generan en tiempo de ejecución aquí. Los transcripts de ejemplo que se almacenan en `my_mock.h` (que representan los usuarios sobre los que se verifica la edad) se crean mediante el script `generate.py` ubicado en el proyecto `mdoc_generator`. Si necesitas regenerar esos transcripts, ejecuta `generate.py` en `mdoc_generator` y actualiza `my_mock.h` con los resultados.

Siguiente paso propuesto: generar los transcripts firmando el CBOR con la clave interna de Android. Técnicamente esto implicaría producir el CBOR del transcript y firmarlo usando la clave privada almacenada en el `AndroidKeyStore` del dispositivo, de forma que la verificación pueda comprobar tanto la integridad del transcript como su origen seguro. Esta operación tiene implicaciones de seguridad y permisos (uso de KeyStore, acceso a claves no exportables, y flujo de consentimiento del usuario) y por eso se describe aquí como una guía para implementarlo posteriormente.
```

---

## Componentes Principales

### 1. **MainActivity.kt**

**Responsabilidad**: Punto de entrada de la aplicación y manejo de resultados Intent.

```kotlin
class MainActivity : ComponentActivity() {
    // ViewModels para gestionar estado
    private val settingsViewModel: SettingsViewModel by viewModels { appViewModelFactory }
    private val walletViewModel: WalletViewModel by viewModels { appViewModelFactory }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Configura UI con Compose
        setContent {
            NavBar(
                // ...
                onProofReady = { resultPair ->
                    if (resultPair != null) {
                        // Prueba generada exitosamente
                        val resultIntent = Intent().apply {
                            putExtra("zk_proof", resultPair.first)      // Proof bytes
                            putExtra("zk_transcript", resultPair.second) // Transcript
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish() // Cierra y vuelve a Gurry
                    }
                }
            )
        }
    }
}
```

### 2. **WalletViewModel.kt**

**Responsabilidad**: Lógica de negocio para generación de pruebas.

```kotlin
class WalletViewModel(
    repository: GurryWalletRepository,
    private val cacheDir: String
) : ViewModel() {
    
    init {
        System.loadLibrary("gurry_native") // Carga JNI
    }
    
    // Interfaz JNI: Declara la función nativa
    private external fun ejecutarPruebaZkNativa(
        indiceCredencial: Int, 
        cacheDir: String
    ): Array<ByteArray>?
    
    // Ejecuta generación de prueba en Dispatcher.IO
    suspend fun generarPruebaEdad(indice: Int): Pair<ByteArray, ByteArray>? {
        return withContext(Dispatchers.IO) {
            val result = ejecutarPruebaZkNativa(indice, cacheDir)
            if (result != null && result.size == 2) {
                Pair(result[0], result[1])  // (proof, transcript)
            } else {
                null
            }
        }
    }
}
```

### 3. **WalletScreen.kt**

**Responsabilidad**: UI que muestra credenciales y dispara generación de pruebas.

```kotlin
@Composable
fun WalletContent(
    onRunCredential: suspend (Int) -> Pair<ByteArray, ByteArray>?,
    credentials: List<CredentialEntity>,
    onProofGenerated: (Pair<ByteArray, ByteArray>?) -> Unit
) {
    // Renderiza tarjetas de credencial
    LazyColumn {
        items(credentials) { credencial ->
            CredentialCard(
                entity = credencial,
                onCardClick = {
                    // Genera prueba en corrutina
                    scope.launch {
                        val result = onRunCredential(credencial.indexEnC)
                        onProofGenerated(result)  // Callback a MainActivity
                    }
                }
            )
        }
    }
}
```

### 4. **Base de Datos (Room)**

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String  // "u_raul", "u_lucas"
)

@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val idCredencial: Int = 0,
    val userId: String,
    val tipo: TypeOfCredential,
    val indexEnC: Int,          // Índice en el array C de credenciales
    val credPicture: String,
    val country: String
)

enum class TypeOfCredential {
    DNI, DRIVER_LICENSE, HEALTH_CARD, PROFESSIONAL_CARD, YOUTH_CARD
}
```

---

## Generación de Pruebas ZK

### ¿Cómo Funciona el Sistema de Pruebas?

La generación de pruebas ZK se basa en **Longfellow-ZK**, un sistema criptográfico que permite probar proposiciones complejas sin revelar datos privados.

### 1. **Sesión (Session Transcript)**

Una sesión es un identificador único y reproducible que enlaza:

```
Session Transcript = CBOR(
    client_id,          // "Aplicación Gurry"
    server_id,          // "Servidor de Verificación"
    session_id,         // UUID único de esta solicitud
    timestamp,          // Momento de la solicitud
    challenges[]        // Desafíos criptográficos
)

Ejemplo:
{
  "client": "com.gurry.verifier",
  "server": "gurry-verification-server",
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-05-13T14:30:00Z",
  "challenge": [0x12, 0x34, 0x56, ...]
}
```

**Propósito**: 
- Evita replay attacks (una prueba no sirve para otra sesión)
- Vincula la prueba al contexto específico
- Todos los interlocutores tienen el mismo transcript

### 2. **Generación de la Prueba (run_mdoc_prover)**

El proceso completo es:

```
Input:
  ├─ Circuit: Especificación ZK del sistema
  ├─ mDL: Credencial ISO 18013-5 (e.g., Carnet de conducir)
  │   └─ Estructura:
  │       {
  │         "issuerSignedData": { /* datos del emisor */ },
  │         "deviceSignedData": { /* datos del dispositivo */ },
  │         "attributes": {
  │             "family_name": "Pérez",
  │             "birth_date": "1990-03-15",
  │             "driving_privileges": [...]
  │         }
  │       }
  ├─ Issuer Public Key: Clave pública del emisor
  ├─ Transcript: Sesión actual (CBOR)
  ├─ Requested Attributes: ["birth_date", "family_name"]
  └─ ZK Spec: Versión del sistema

Process:
  1. Valida firma del emisor sobre la credencial
  2. Extrae los atributos solicitados
  3. Inicializa el circuito con:
     - Los atributos privados
     - El pública información (emisor, tipos)
     - La sesión (transcript)
  4. Ejecuta protocolo Longfellow-ZK en 3 fases:
     
     PHASE 1 - Commitment:
       - Prover genera números aleatorios privados
       - Crea commitments criptográficos
       - Se envían al verifier
       
     PHASE 2 - Sumcheck Protocol:
       - Prover y verifier interactúan
       - Verifier desafía: "¿Puedes probar que tu claim es correcto?"
       - Prover responde con pruebas de evaluaciones polinomiales
       - En cada ronda, la complejidad disminuye
       
     PHASE 3 - Low Degree Proof (Ligero):
       - Prover genera código de corrección de errores
       - Demostraea que una suma de evaluaciones es correcta
       - Usa pruebas probabilísticas cortas
       
  5. Retorna: Proof (bytes serializados)

Output:
  └─ ZKDeviceResponseCBOR = CBOR(
       "proof": [commitments, sumcheck_messages, ligero_proof],
       "statement": {
           "issuer_pubkey": {...},
           "attributes_commitment": "0x...",
           "doc_type": "org.iso.18013.5.1.mDL"
       }
     )
```

### 3. **Verificación (run_mdoc_verifier)**

El servidor recibe proof + transcript y:

```
Input:
  ├─ Proof: Generada por el prover (GurryWallet)
  ├─ Transcript: Session transcript CBOR
  ├─ Circuit: Especificación del sistema
  ├─ Issuer Public Key
  └─ Attributes Requested

Verify Process:
  1. Descomprime la prueba
  2. Verifica que el transcript es el esperado
  3. Ejecuta verificación del circuito:
     
     PHASE 1 - Check Commitment:
       - Verifica que el commitment es válido
       
     PHASE 2 - Sumcheck Verification:
       - Para cada ronda i:
         * Verifier genera challenge c_i
         * Verifica que la respuesta del prover es consistente
         * Reduce el problema a un problema más pequeño
         
     PHASE 3 - Low Degree Test:
       - Verifica que el código es efectivamente de baja densidad
       - Usa evaluaciones aleatorias
       
  4. Si todas las verificaciones pasan:
     └─ Retorna: VERIFIED + atributos extraídos
        {
          "status": true,
          "claims": {
            "birth_date": "1990-03-15",
            "family_name": "Pérez",
            "issuer": "ES_Ministry_Interior"
          }
        }
  5. Si falla:
     └─ Retorna: FAILED + motivo del error
```

### 4. **Especificación ZK (ZkSpec)**

El sistema soporta 12 versiones de ZK specs, cada una con parámetros:

```cpp
struct ZkSpecStruct {
    size_t version;              // Versión (1-12)
    size_t block_enc_hash;       // Parámetro de encriptación
    size_t block_enc_sig;        // Parámetro de firma
};

// Hardcodeadas en el motor
extern const ZkSpecStruct kZkSpecs[kNumZkSpecs];

// El prover elige la versión según:
// - Atributos a probar
// - Campo finito (Fp256 vs F_128)
// - Nivel de seguridad requerido
```

---

## Integración con Gurry

### Protocolo de Comunicación

#### 1. **Gurry solicita Prueba**

```kotlin
// En la app Gurry:
val proofRequestIntent = Intent().apply {
    action = "com.example.gurrywallet.REQUEST_ZK_PROOF"
    putExtra("credential_type", "DRIVER_LICENSE")      // Tipo de credencial
    putExtra("attributes", arrayOf("birth_date", "family_name"))
    putExtra("session_id", "550e8400-e29b-41d4-a716")
    putExtra("challenge", challengeBytes)
}

startActivityForResult(proofRequestIntent, REQUEST_CODE_PROOF)
```

#### 2. **GurryWallet Genera Prueba**

- User ve credenciales en GurryWallet
- Selecciona la credencial a usar
- App llama JNI para generar prueba (con los datos de sesión)
- Retorna proof + transcript

#### 3. **Gurry Recibe Respuesta**

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (requestCode == REQUEST_CODE_PROOF && resultCode == Activity.RESULT_OK) {
        val proof = data?.getByteArrayExtra("zk_proof")
        val transcript = data?.getByteArrayExtra("zk_transcript")
        
        // Envía al servidor para verificación
        verifyProofOnServer(proof, transcript)
    }
}

private fun verifyProofOnServer(proof: ByteArray, transcript: ByteArray) {
    val request = JSONObject().apply {
        put("ZKDeviceResponseCBOR", Base64.getEncoder().encodeToString(proof))
        put("Transcript", Base64.getEncoder().encodeToString(transcript))
    }
    
    // POST a servidor de verificación
    val client = OkHttpClient()
    val req = Request.Builder()
        .url("https://gurry-server/zkverify")
        .post(RequestBody.create("application/json".toMediaType(), request.toString()))
        .build()
    
    client.newCall(req).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) { /* Error */ }
        
        override fun onResponse(call: Call, response: Response) {
            val responseJson = JSONObject(response.body?.string() ?: "{}")
            if (responseJson.getBoolean("Status")) {
                // Verificación exitosa
                val claims = responseJson.getJSONObject("Claims")
                // Procesar claims verificados
            }
        }
    })
}
```

### Flujo Visual

```
┌──────────────────┐
│      GURRY       │
│   (App Cliente)  │
└────────┬─────────┘
         │ Intent with:
         │ - Credential type
         │ - Attributes to prove
         │ - Session ID + Challenge
         │
         ▼
┌──────────────────────────┐
│   GURRYWALLET            │
│  - Show credentials      │
│  - User selects cred     │
│  - Generate ZK proof     │
└────────┬────────────────┘
         │ Intent Result with:
         │ - ZK Proof bytes
         │ - Transcript bytes
         │
         ▼
┌──────────────────────────┐
│   GURRY VERIFICATION     │
│   SERVER (Backend)       │
│  - Verify proof          │
│  - Return claims         │
└──────────────────────────┘
```

---

## Construcción y Ejecución

### Requisitos Previos

```
- Android Studio (Koala o superior)
- Android NDK (para compilar C++)
- Gradle 8.2+
- Java 17+
- CMake 3.22+
```

### Compilación

#### 1. **Clonar el Repositorio**

```bash
git clone https://github.com/rauldz/GurryWallet.git
cd GurryWallet
```

#### 2. **Compilar Proyecto Android + Nativo**

```bash
# Build completo (Gradle + CMake)
./gradlew build

# O build debug específico
./gradlew assembleDebug

# Clean si hay problemas
./gradlew clean && ./gradlew build
```

#### 3. **Instalar en Dispositivo/Emulador**

```bash
# Conectar dispositivo Android o iniciar emulador

# Instalar APK
./gradlew installDebug

# O usar adb directamente
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Ver logs
adb logcat | grep "GURRY\|ZkTest"
```

### Estructura de Directorios Relevantes

```
GurryWallet/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/gurrywallet/
│   │       │   ├── MainActivity.kt                 # Punto de entrada
│   │       │   ├── WalletViewModel.kt             # Lógica principal
│   │       │   ├── WalletScreen.kt                # UI
│   │       │   ├── GurryWalletRepository.kt       # Acceso a datos
│   │       │   ├── database/
│   │       │   │   ├── WalletDao.kt               # Room DAO
│   │       │   │   ├── CredentialEntity.kt        # Modelo credencial
│   │       │   │   └── UserEntity.kt              # Modelo usuario
│   │       │   └── components/
│   │       │       └── NavBar.kt                   # Navegación UI
│   │       ├── cpp/                               # Código C++ nativo
│   │       │   └── longfellow-zk/
│   │       │       ├── lib/
│   │       │       │   ├── circuits/mdoc/
│   │       │       │   │   ├── mdoc_zk.h          # API principal ZK
│   │       │       │   │   └── (implementación)
│   │       │       │   ├── zk/
│   │       │       │   │   ├── zk_prover.h        # Generador de pruebas
│   │       │       │   │   └── zk_verifier.h      # Verificador
│   │       │       │   └── (librerías auxiliares)
│   │       │       └── reference/
│   │       │           └── verifier-service/
│   │       │               ├── server/
│   │       │               │   ├── main.go         # Servidor verificación
│   │       │               │   └── handler.go      # Endpoints HTTP
│   │       │               └── v2/zk/
│   │       │                   └── (lógica servidor)
│   │       ├── AndroidManifest.xml
│   │       └── res/
│   └── build.gradle.kts                            # Configuración Gradle
│
├── gradle/
│   └── libs.versions.toml                          # Versionado de deps
├── settings.gradle.kts
└── local.properties                                # NDK + SDK paths
```

### Configuración Necesaria

#### 1. **local.properties**

```properties
# Ruta del SDK de Android
sdk.dir=/Users/usuario/Library/Android/sdk

# Ruta del NDK (necesario para compilar C++)
ndk.dir=/Users/usuario/Library/Android/sdk/ndk/25.2.9519653
```

#### 2. **CMakeLists.txt**

Configura la compilación de código C++ nativo:

```cmake
cmake_minimum_required(VERSION 3.22)
project(gurry_native)

# Incluye headers de Longfellow-ZK
include_directories(
    app/src/main/cpp/longfellow-zk/lib/circuits/mdoc
    app/src/main/cpp/longfellow-zk/lib/zk
)

# Agrega la librería nativa
add_library(
    gurry_native SHARED
    app/src/main/cpp/gurry_native.cpp
)

# Linkea con librerías Longfellow-ZK
target_link_libraries(
    gurry_native
    mdoc_zk
    zk_prover
    zk_verifier
    crypto
)
```

#### 3. **Diagrama de Compilación**

```
GurryWallet/
    │
    ├─→ Kotlin + Compose (Android)
    │       └─→ Gradle compila → APK
    │
    └─→ C++ (Longfellow-ZK)
            ├─→ CMake escanea fuentes
            ├─→ Compilador C++ (NDK) genera .o
            └─→ Linker crea libgurry_native.so
                    └─→ Incluida en APK
```

---

## Flujo Técnico Detallado: De Intent a Prueba

### Secuencia Completa de Ejecución

```
┌──────────────────────────────────────────────────────────┐
│ USUARIO: Abre Gurry, solicita verificación de edad      │
└──────────────────────────────────────────────────────────┘

Gurry crea Intent:
  ├─ action = "com.example.gurrywallet.REQUEST_PROOF"
  ├─ credential_type = "DRIVER_LICENSE"
  ├─ attributes = ["birth_date"]
  └─ session_data = SessionTranscript (CBOR encoded)

                    ▼

┌──────────────────────────────────────────────────────────┐
│ GURRYWALLET: MainActivity.onCreate() recibe Intent      │
└──────────────────────────────────────────────────────────┘

1. WalletScreen se renderiza
2. Muestra credenciales disponibles del usuario actual

┌──────────────────────────────────────────────────────────┐
│ USUARIO: Selecciona credencial (DNI/Carnet conductor)   │
└──────────────────────────────────────────────────────────┘

CredentialCard.onCardClick()
  └─→ scope.launch { onRunCredential(credencial.indexEnC) }

                    ▼

┌──────────────────────────────────────────────────────────┐
│ WALLETVIEWMODEL: generarPruebaEdad(index)               │
└──────────────────────────────────────────────────────────┘

fun generarPruebaEdad(indice: Int): Pair<ByteArray, ByteArray>? {
    return withContext(Dispatchers.IO) {  // En hilo background
        // Llama a función nativa (JNI)
        val result = ejecutarPruebaZkNativa(indice, cacheDir)
        
        if (result != null && result.size == 2) {
            return Pair(result[0], result[1])  // (proof, transcript)
        }
        null
    }
}

                    ▼

┌──────────────────────────────────────────────────────────┐
│ JNI INTERFACE: Entra en código C++ nativo               │
└──────────────────────────────────────────────────────────┘

JNIEXPORT jobjectArray JNICALL
Java_com_example_gurrywallet_WalletViewModel_ejecutarPruebaZkNativa(
    JNIEnv* env,
    jobject obj,
    jint credentialIndex,
    jstring cacheDir
) {
    // 1. Obtiene contexto JNI
    const char* cache_path = env->GetStringUTFChars(cacheDir, 0);
    
    // 2. Carga credencial desde Room DB
    //    (Ya está almacenada localmente)
    auto credential = load_credential_from_db(credentialIndex);
    
    // 3. Deserializa mDL de la credencial
    //    - Estructura ISO 18013-5
    //    - Incluye datos personales + firma del emisor
    uint8_t* mdoc_bytes = credential.mdoc_data;
    size_t mdoc_len = credential.mdoc_len;
    
    // 4. Carga el circuito ZK del proyecto
    uint8_t* circuit = load_circuit_from_assets();
    size_t circuit_len = circuit_size();
    
    // 5. Obtiene clave pública del emisor
    const char* issuer_pk_x = credential.issuer_pk_x;
    const char* issuer_pk_y = credential.issuer_pk_y;
    
    // 6. Lee el transcript de sesión (del Intent de Gurry)
    uint8_t* transcript = read_session_transcript(cache_path);
    size_t tr_len = transcript_size();
    
    // 7. Define qué atributos queremos probar
    RequestedAttribute attrs[] = {
        {"birth_date", 10}  // Solo probamos que existe
    };
    
    // 8. Selecciona la versión de ZK spec
    const ZkSpecStruct* zk_spec = &kZkSpecs[LATEST_VERSION];
    
    // 9. Llama al prover de Longfellow-ZK
    uint8_t* proof = nullptr;
    size_t proof_len = 0;
    
    MdocProverErrorCode error = run_mdoc_prover(
        circuit, circuit_len,
        mdoc_bytes, mdoc_len,
        issuer_pk_x, issuer_pk_y,
        transcript, tr_len,
        attrs, 1,               // 1 atributo a probar
        "2025-05-13T14:30:00Z", // Timestamp actual
        &proof, &proof_len,
        zk_spec
    );
    
    if (error != MDOC_PROVER_SUCCESS) {
        // Error en generación
        return nullptr;
    }
    
    // 10. Retorna al código Kotlin
    //     Array de 2 elementos: [proof, transcript]
    jobjectArray result = env->NewObjectArray(2, 
        env->FindClass("[B"), nullptr);
    
    jbyteArray proofArray = env->NewByteArray(proof_len);
    env->SetByteArrayRegion(proofArray, 0, proof_len, (jbyte*)proof);
    env->SetObjectArrayElement(result, 0, proofArray);
    
    jbyteArray transcriptArray = env->NewByteArray(tr_len);
    env->SetByteArrayRegion(transcriptArray, 0, tr_len, (jbyte*)transcript);
    env->SetObjectArrayElement(result, 1, transcriptArray);
    
    // Limpia memoria
    free(proof);
    free(transcript);
    
    return result;
}

                    ▼

┌──────────────────────────────────────────────────────────┐
│ PROVER (C++ Longfellow-ZK): run_mdoc_prover()           │
└──────────────────────────────────────────────────────────┘

// En mdoc_zk.cc:

MdocProverErrorCode run_mdoc_prover(
    const uint8_t* circuit,
    size_t circuit_len,
    const uint8_t* mdoc,
    size_t mdoc_len,
    const char* issuer_pk_x,
    const char* issuer_pk_y,
    const uint8_t* transcript,
    size_t transcript_len,
    const RequestedAttribute* attributes,
    size_t attributes_count,
    const char* now,
    uint8_t** proof_out,
    size_t* proof_len_out,
    const ZkSpecStruct* zk_spec
) {
    try {
        // STEP 1: Valida credencial
        if (!validate_mdoc_signature(mdoc, issuer_pk_x, issuer_pk_y)) {
            return MDOC_PROVER_ERROR_INVALID_SIGNATURE;
        }
        
        // STEP 2: Descomprime y carga el circuito
        Circuit<Fp256> circuit_obj = decompress_circuit(circuit, circuit_len);
        
        // STEP 3: Inicializa Fiat-Shamir transcript
        Transcript transcript_fs;
        transcript_fs.init(transcript, transcript_len);
        
        // STEP 4: Crea el prover ZK
        ReedSolomonFactory rs_factory(zk_spec->rate);
        ZkProver<Fp256, ReedSolomonFactory> prover(
            circuit_obj,
            Fp256::field(),
            rs_factory
        );
        
        // STEP 5: Inicializa witness con datos privados
        //         (solo los atributos solicitados)
        Dense<Fp256> witness = extract_witness(mdoc, attributes, attributes_count);
        
        // STEP 6: Fase 1 - Commitment
        //         Prover genera números aleatorios y commitments
        ZkProof<Fp256> zk_proof;
        RandomEngine rng;
        prover.commit(zk_proof, witness, transcript_fs, rng);
        
        // STEP 7: Fase 2 & 3 - Sumcheck + Ligero
        //         Protocolo interactivo de prueba
        if (!prover.prove(zk_proof, witness, transcript_fs)) {
            return MDOC_PROVER_ERROR_PROOF_GENERATION_FAILED;
        }
        
        // STEP 8: Serializa la prueba a CBOR
        std::vector<uint8_t> proof_bytes = serialize_proof_to_cbor(zk_proof);
        
        // STEP 9: Retorna resultado
        *proof_out = new uint8_t[proof_bytes.size()];
        memcpy(*proof_out, proof_bytes.data(), proof_bytes.size());
        *proof_len_out = proof_bytes.size();
        
        return MDOC_PROVER_SUCCESS;
        
    } catch (const std::exception& e) {
        return MDOC_PROVER_ERROR_EXCEPTION;
    }
}

                    ▼

┌──────────────────────────────────────────────────────────┐
│ KOTLIN: Recibe resultado de JNI                          │
└──────────────────────────────────────────────────────────┘

// En WalletViewModel.kt
val result = ejecutarPruebaZkNativa(indice, cacheDir)
// result = Array<ByteArray>[proof_bytes, transcript_bytes]

return Pair(result[0], result[1])

                    ▼

┌──────────────────────────────────────────────────────────┐
│ MAINACTIVITY: Recibe Pair y prepara Intent resultado    │
└──────────────────────────────────────────────────────────┘

// En MainActivity.kt
onProofReady = { resultPair ->
    if (resultPair != null) {
        val resultIntent = Intent().apply {
            putExtra("zk_proof", resultPair.first)
            putExtra("zk_transcript", resultPair.second)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()  // Cierra GurryWallet y vuelve a Gurry
    }
}

                    ▼

┌──────────────────────────────────────────────────────────┐
│ GURRY: Recibe resultado en onActivityResult()           │
└──────────────────────────────────────────────────────────┘

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CODE_ZK_PROOF && resultCode == Activity.RESULT_OK) {
        val proof = data?.getByteArrayExtra("zk_proof")
        val transcript = data?.getByteArrayExtra("zk_transcript")
        
        // Envía al servidor para verificación
        verifyProof(proof, transcript)
    }
}

                    ▼

┌──────────────────────────────────────────────────────────┐
│ SERVIDOR GURRY (Go): Verifica prueba con run_mdoc_verifier │
└──────────────────────────────────────────────────────────┘

// En verifier-service/server/handler.go
func (s *Server) handleZKVerify(w http.ResponseWriter, r *http.Request) error {
    var request ZKVerifyRequest
    json.NewDecoder(r.Body).Decode(&request)
    
    // Procesa la respuesta CBOR del device
    vreq, err := zk.ProcessDeviceResponse(request.ZKDeviceResponseCBOR)
    vreq.Transcript = request.Transcript
    
    // Verifica la prueba
    ok, err := zk.VerifyProofRequest(vreq)
    
    response := ZKVerifyResponse{
        Status: ok,
        Claims: vreq.Claims,
    }
    
    if err != nil {
        response.Message = err.Error()
    }
    
    return writeJSON(w, http.StatusOK, response)
}

// En zk/verifier.go (C++ llamado desde Go)
bool VerifyProofRequest(VerifyRequest* vreq) {
    MdocVerifierErrorCode result = run_mdoc_verifier(
        circuit, circuit_len,
        issuer_pk_x, issuer_pk_y,
        vreq->transcript, vreq->transcript_len,
        attributes, attributes_count,
        now,
        vreq->proof, vreq->proof_len,
        "org.iso.18013.5.1.mDL",
        &zk_spec
    );
    
    return result == MDOC_VERIFIER_SUCCESS;
}

                    ▼

┌──────────────────────────────────────────────────────────┐
│ VERIFICACIÓN EXITOSA: Atributos validados               │
└──────────────────────────────────────────────────────────┘

{
  "Status": true,
  "Claims": {
    "birth_date": "1990-03-15",
    "family_name": "Pérez García",
    "issuer": "es.gob.mir",
    "is_older_than_18": true  // ✅ VERIFICADO sin revelar fecha exacta
  }
}
```

---

## Conclusión

GurryWallet es una **billetera de credenciales con privacidad avanzada** que permite a los usuarios generar pruebas criptográficas sin revelar información sensible. Su arquitectura de dos capas (Kotlin + C++ Longfellow-ZK) proporciona:

- 🔐 **Seguridad Criptográfica**: Pruebas zero-knowledge verificables
- 📱 **Experiencia de Usuario**: UI moderna en Compose
- 🔄 **Integración Fluida**: Comunicación Intent con aplicaciones cliente
- ⚡ **Rendimiento**: Operaciones nativas optimizadas

El flujo completo permite que aplicaciones como Gurry verifiquen información del usuario sin acceso a datos sensibles, mejorando la privacidad digital en toda la cadena.

---

## Referencias

- [ISO/IEC 18013-5:2021](https://www.iso.org/standard/69084.html) - Personal identification
- [OpenID4VP](https://openid.net/specs/openid-4-verifiable-presentations-1_0.html) - Verifiable Presentations
- [Longfellow-ZK](https://github.com/google/privacy-proofs/tree/main/longfellow-zk) - Zero-Knowledge Proofs
- [Android JNI](https://developer.android.com/training/articles/on-device-ml/interpreter-api) - Java Native Interface
