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

// =====================================================================
// CORE LOGIC: PREPARADO PARA KOTLIN / JNI
// =====================================================================
int ejecutar_prueba_zk(int indice_credencial, uint8_t* circuito, size_t longitud_circuito) {
    if (indice_credencial < 0 || indice_credencial > 1) {
        return -1;
    }

    const MdocTests& cred = custom_mock::credenciales_mock[indice_credencial];
    const RequestedAttribute attrs[] = { test::age_over_18 };

    uint8_t* proof = nullptr;
    size_t proof_len = 0;

    LOGI("--- [PASO 1] INICIANDO PROVER (Wallet) ---");
    MdocProverErrorCode ret_prover = run_mdoc_prover(
            circuito, longitud_circuito,
            cred.mdoc, cred.mdoc_size,
            cred.pkx.as_pointer, cred.pky.as_pointer,
            cred.transcript, cred.transcript_size,
            attrs, 1,
            (const char*)cred.now,
            &proof, &proof_len,
            &kZkSpecs[0]
    );

    LOGI("--- [PASO 2] INICIANDO VERIFIER (Discoteca) ---");

    MdocVerifierErrorCode ret_verifier = run_mdoc_verifier(
            circuito, longitud_circuito,
            cred.pkx.as_pointer, cred.pky.as_pointer,
            cred.transcript, cred.transcript_size,
            attrs, 1,
            (const char*)cred.now,
            proof, proof_len,
            cred.doc_type,
            &kZkSpecs[0]
    );

    if (proof) free(proof);

    if (ret_verifier == MDOC_VERIFIER_SUCCESS) {
        LOGI(">>> EXITO: ¡El Verifier confirma que la prueba es legitima!");
        return 1;
    } else {
        LOGE(">>> ERROR: El Verifier ha rechazado la prueba (Codigo: %d).", ret_verifier);
        return 0;
    }
}

extern "C" JNIEXPORT jint  JNICALL
Java_com_example_gurrywallet_WalletViewModel_ejecutarPruebaZkNativa(JNIEnv *env, jobject thiz, jint indice_credencial) {

    // 1. Generar el circuito la primera vez
    if (!circuit_) {
        LOGI("Compilando el circuito ZK por primera vez en Android...");
        generate_circuit(&kZkSpecs[0], &circuit_, &len_);
        LOGI("Circuito compilado correctamente.");
    }

    // 2. Ejecutar tu lógica
    return ejecutar_prueba_zk(indice_credencial, circuit_, len_);
}