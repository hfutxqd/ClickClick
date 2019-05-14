/*
 *
 *  AndroidKeyInjector
 *
 *  Copyright (C) 2012 Radu Motisan , www.pocketmagic.net
 *
 *  radu.motisan@gmail.com
 *
 */

#include <string.h>
#include <jni.h>

#include <stdlib.h>
#include <unistd.h>

#include <fcntl.h>
#include <stdio.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <time.h>

#include <linux/fb.h>
#include <linux/kd.h>


#include "uinput.h"

#include <android/log.h>
#define TAG "JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

/* Debug tools
 */
 int g_debug = 0;

void debug(char *szFormat, ...)
{
	if (g_debug == 0) return;
	//if (strlen(szDbgfile) == 0) return;

	char szBuffer[4096]; //in this buffer we form the message
	const size_t NUMCHARS = sizeof(szBuffer) / sizeof(szBuffer[0]);
	const int LASTCHAR = NUMCHARS - 1;
	//format the input string
	va_list pArgs;
	va_start(pArgs, szFormat);
	// use a bounded buffer size to prevent buffer overruns.  Limit count to
	// character size minus one to allow for a NULL terminating character.
	vsnprintf(szBuffer, NUMCHARS - 1, szFormat, pArgs);
	va_end(pArgs);
	//ensure that the formatted string is NULL-terminated
	szBuffer[LASTCHAR] = '\0';

    //LOGD(szBuffer);
}

jint Java_net_pocketmagic_keyinjector_NativeInput_intEnableDebug( JNIEnv* env,jobject thiz, jint enable ) {

	g_debug = enable;
	if (enable == 1) debug("Debug enabled.");
	return g_debug;
}

jint Java_net_pocketmagic_keyinjector_NativeInput_intCreate( JNIEnv* env,jobject thiz, jstring inputdev, jint keyboard, jint mouse)
{

	jboolean iscopy;
	char szDev[255] = "";
	const char *pszDev = (*env)->GetStringUTFChars(env, inputdev, &iscopy);
	if (pszDev) strncpy(szDev, pszDev, 255);
	(*env)->ReleaseStringUTFChars(env, inputdev, pszDev);
	debug("intCreate call (%s)", szDev);

	struct uinput_dev dev;
	int fd_kb, aux;

	fd_kb = open(szDev, O_RDWR);
	if (fd_kb < 0) {
		debug("Can't open input device:%s ", szDev);
		return -1;
	}

	memset(&dev, 0, sizeof(dev));
	strcpy(dev.name, "AndroidKeyInjector Input");
	dev.id.bustype = 0x0003;// BUS_USB;
	dev.id.vendor  = 0x0000;
	dev.id.product = 0x0000;
	dev.id.version = 0x0000;

	if (write(fd_kb, &dev, sizeof(dev)) < 0) {
		debug("Can't write device information");
		close(fd_kb);
		return -1;
	}

	if (mouse) {
		ioctl(fd_kb, UI_SET_EVBIT, EV_REL);
		for (aux = REL_X; aux <= REL_MISC; aux++)
			ioctl(fd_kb, UI_SET_RELBIT, aux);
	}

	if (keyboard) {
		ioctl(fd_kb, UI_SET_EVBIT, EV_KEY);
		ioctl(fd_kb, UI_SET_EVBIT, EV_LED);
		ioctl(fd_kb, UI_SET_EVBIT, EV_REP);

		for (aux = KEY_RESERVED; aux <= KEY_UNKNOWN; aux++)
			ioctl(fd_kb, UI_SET_KEYBIT, aux);

		//for (aux = LED_NUML; aux <= LED_MISC; aux++)
		//	ioctl(fd_kb, UI_SET_LEDBIT, aux);
	}

	if (mouse) {
		ioctl(fd_kb, UI_SET_EVBIT, EV_KEY);

		for (aux = BTN_LEFT; aux <= BTN_BACK; aux++)
			ioctl(fd_kb, UI_SET_KEYBIT, aux);
	}

	ioctl(fd_kb, UI_DEV_CREATE);
	debug("intCreate success: %d",  fd_kb);
	return fd_kb;
}

void  Java_net_pocketmagic_keyinjector_NativeInput_intClose( JNIEnv* env,jobject thiz, jint fd_kb)
{
	close(fd_kb);
}

void Java_net_pocketmagic_keyinjector_NativeInput_intSendEvent( JNIEnv* env,jobject thiz, int fd_kb, uint16_t type, uint16_t code, int32_t value)
{
	debug("intSendEvent call (%d,%d,%d,%d)", fd_kb, type, code, value);
	struct uinput_event event;
	int len;

	if (fd_kb <= fileno(stderr))
		return;

	memset(&event, 0, sizeof(event));
	event.type = type;
	event.code = code;
	event.value = value;

	len = write(fd_kb, &event, sizeof(event));
	debug("intSendEvent done:%d",len);
}


jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	debug("native lib loaded.");
	return JNI_VERSION_1_2; //1_2 1_4
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{
	debug("native lib unloaded.");
}