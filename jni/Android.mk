LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := hdtuner
LOCAL_SRC_FILES := hdtuner.cpp hdcontrol.cpp hddefs.cpp hdlinuxio.cpp hdcommands.cpp hdlisten.cpp
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
