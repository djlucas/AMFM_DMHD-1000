#include <jni.h>
#include <string>
#include <android/log.h>
#include "hdtuner.h"
#include "hdcontrol.h"

using namespace std;

HDControl hdc;
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"RADIO",__VA_ARGS__)

JNIEXPORT void JNICALL Java_tk_rabidbeaver_radio_RadioWrapper_initRadio (JNIEnv * env, jobject jo){
	hdc.activate();
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
	JNIEnv * env;
	LOGD("RUNNING JNI_OnLoad");
	vm->AttachCurrentThread(&env,NULL);
	jclass s_jcls = (jclass)env->NewGlobalRef(env->FindClass("tk/rabidbeaver/radio/RadioWrapper"));
	if (vm == NULL) LOGD("WHY IS IT NULL?");
	hdc.passJvm(vm, s_jcls);

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_tk_rabidbeaver_radio_RadioWrapper_deinitRadio (JNIEnv * env, jclass jc){
	hdc.close();
}

JNIEXPORT void JNICALL Java_tk_rabidbeaver_radio_RadioWrapper_sendCommand (JNIEnv * env, jclass jc, jstring cmdline){
	hdc.command_line(string(env->GetStringUTFChars(cmdline,0)));
}
