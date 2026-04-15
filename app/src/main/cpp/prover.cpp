#include <jni.h>
#include <malloc.h>
#include <android/log.h> // Sustituye a <stdio.h> para imprimir en Android
#include <stdio.h>    // Para FILE, fopen, fread, fwrite
#include <string>

// Rutas limpias. El CMake se encargará de encontrar la carpeta "lib"
#include "circuits/mdoc/mdoc_zk.h"
#include "circuits/mdoc/mdoc_examples.h"
#include "circuits/mdoc/mdoc_test_attributes.h"
#include "circuits/mdoc/my_mock.h"

using namespace proofs;

// Variables globales estáticas (Sustituyen a la antigua clase TfgAgeTest)
static uint8_t *circuit_ = nullptr;
static size_t len_ = 0;

// Macro para usar logs de Android en lugar de printf
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "ZkNative", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "ZkNative", __VA_ARGS__)



extern "C" JNIEXPORT jobjectArray  JNICALL
Java_com_example_gurrywallet_WalletViewModel_ejecutarPruebaZkNativa(JNIEnv *env, jobject thiz, jint indice_credencial, jstring cacheDir) {
    if (indice_credencial < 0 || indice_credencial > 1) return nullptr;

    const char *cacheDir_cstr = env->GetStringUTFChars(cacheDir, nullptr);
    std::string file_path = std::string(cacheDir_cstr) + "/circuito_mdoc.bin";
    env->ReleaseStringUTFChars(cacheDir, cacheDir_cstr);

    // 1. Generar el circuito la primera vez y meterlo en caché
    if (!circuit_) {
        // Intentamos abrir el archivo en modo "lectura binaria" (rb)
        FILE* f = fopen(file_path.c_str(), "rb");

        if (f) {
            // EL ARCHIVO EXISTE: Lo leemos (Warm Start ultrarrápido)
            LOGI("✅ Cargando circuito desde caché: %s", file_path.c_str());

            // Averiguar tamaño del archivo
            fseek(f, 0, SEEK_END);
            len_ = ftell(f);
            fseek(f, 0, SEEK_SET);

            // Reservar memoria y volcar datos
            circuit_ = (uint8_t*) malloc(len_);
            fread(circuit_, 1, len_, f);
            fclose(f);

            LOGI("✅ Circuito cargado correctamente desde caché. Tamaño: %zu bytes", len_);
        } else {
            // EL ARCHIVO NO EXISTE: Toca calcularlo (Cold start)
            LOGI("⚠️ Circuito no encontrado en caché. Compilando (esto tardará un poco)...");
            generate_circuit(&kZkSpecs[0], &circuit_, &len_);
            LOGI("✅ Circuito compilado en memoria.");

            // Ahora lo GUARDAMOS en disco para el futuro
            f = fopen(file_path.c_str(), "wb"); // "wb" = write binary
            if (f) {
                fwrite(circuit_, 1, len_, f);
                fclose(f);
                LOGI("💾 Circuito guardado en disco para próximas aperturas.");
            } else {
                LOGE("❌ No se pudo guardar el circuito en caché (Revisa permisos o ruta).");
            }
        }
    }
    const MdocTests& cred = custom_mock::credenciales_mock[indice_credencial];
    const RequestedAttribute attrs[] = { test::age_over_18 };
    uint8_t* proof = nullptr;
    size_t proof_len = 0;

    LOGI("WALLET: Iniciando Prover para generar la prueba...");
    MdocProverErrorCode ret_prover = run_mdoc_prover(
            circuit_, len_,
            cred.mdoc, cred.mdoc_size,
            cred.pkx.as_pointer, cred.pky.as_pointer,
            cred.transcript, cred.transcript_size,
            attrs, 1,
            (const char*)cred.now,
            &proof, &proof_len,
            &kZkSpecs[0]
    );

    if (ret_prover != MDOC_PROVER_SUCCESS || !proof) {
        LOGE("WALLET: El Prover no pudo generar la prueba (ej. Menor de edad). Se enviará prueba vacía.");
        if (proof) free(proof);
        proof = nullptr;
        proof_len = 0; // Forzamos una prueba de tamaño 0
    } else {
        LOGI("WALLET: Prueba ZK generada con %zu bytes.", proof_len);
    }

    // 2. Convertir el puntero C++ en un ByteArray de Kotlin/Java
    jbyteArray proof_array = env->NewByteArray(proof_len);


    if (proof_len > 0 && proof != nullptr) {
        env->SetByteArrayRegion(proof_array, 0, proof_len, reinterpret_cast<const jbyte*>(proof));
        free(proof); // Liberamos la memoria C++
    }

    jbyteArray transcript_array = env->NewByteArray(cred.transcript_size);
    env->SetByteArrayRegion(transcript_array, 0, cred.transcript_size, reinterpret_cast<const jbyte*>(cred.transcript));

    jclass byteArrayClass = env->FindClass("[B");
    jobjectArray result_array = env->NewObjectArray(2, byteArrayClass, nullptr);

    env->SetObjectArrayElement(result_array, 0, proof_array);
    env->SetObjectArrayElement(result_array, 1, transcript_array);

    env->DeleteLocalRef(proof_array);
    env->DeleteLocalRef(transcript_array);
    env->DeleteLocalRef(byteArrayClass);
    return result_array;
}