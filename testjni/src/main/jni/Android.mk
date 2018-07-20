LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := TestJNI
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := -lm -llog
LOCAL_SRC_FILES := \
	com_okay_reader_plugin_utils_JNICall.c \


include $(BUILD_SHARED_LIBRARY)
