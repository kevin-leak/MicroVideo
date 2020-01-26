#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_crabglory_cameravideo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_crabglory_microvideo_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}