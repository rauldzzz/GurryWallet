#include <jni.h>
#include <malloc.h>
#include <android/log.h> // Sustituye a <stdio.h> para imprimir en Android

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



extern "C" JNIEXPORT jbyteArray  JNICALL
Java_com_example_gurrywallet_WalletViewModel_ejecutarPruebaZkNativa(JNIEnv *env, jobject thiz, jint indice_credencial) {
    if (indice_credencial < 0 || indice_credencial > 1) return nullptr;
    // 1. Generar el circuito la primera vez
    if (!circuit_) {
        LOGI("Compilando el circuito ZK por primera vez en Android...");
        generate_circuit(&kZkSpecs[0], &circuit_, &len_);
        LOGI("Circuito compilado correctamente.");
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
        LOGE("WALLET: Error al generar la prueba.");
        if (proof) free(proof);
        return nullptr;
    }

    LOGI("WALLET: Prueba ZK generada con %zu bytes.", proof_len);

    // 2. Convertir el puntero C++ en un ByteArray de Kotlin/Java
    jbyteArray proof_array = env->NewByteArray(proof_len);
    env->SetByteArrayRegion(proof_array, 0, proof_len, reinterpret_cast<const jbyte*>(proof));

    free(proof); // Liberamos la memoria C++
    return proof_array;
}