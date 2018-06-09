/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class sidblaster_builder_SIDBlasterSID */

#ifndef _Included_sidblaster_builder_SIDBlasterSID
#define _Included_sidblaster_builder_SIDBlasterSID
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     sidblaster_builder_SIDBlasterSID
 * Method:    HardSID_DeviceCount
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_sidblaster_1builder_SIDBlasterSID_HardSID_1DeviceCount
  (JNIEnv *, jobject);

/*
 * Class:     sidblaster_builder_SIDBlasterSID
 * Method:    HardSID_SIDCount
 * Signature: (B)B
 */
JNIEXPORT jbyte JNICALL Java_sidblaster_1builder_SIDBlasterSID_HardSID_1SIDCount
  (JNIEnv *, jobject, jbyte);

/*
 * Class:     sidblaster_builder_SIDBlasterSID
 * Method:    HardSID_Read
 * Signature: (BBSB)B
 */
JNIEXPORT jbyte JNICALL Java_sidblaster_1builder_SIDBlasterSID_HardSID_1Read
  (JNIEnv *, jobject, jbyte, jbyte, jshort, jbyte);

/*
 * Class:     sidblaster_builder_SIDBlasterSID
 * Method:    HardSID_Write
 * Signature: (BBSBB)V
 */
JNIEXPORT void JNICALL Java_sidblaster_1builder_SIDBlasterSID_HardSID_1Write
  (JNIEnv *, jobject, jbyte, jbyte, jshort, jbyte, jbyte);

/*
 * Class:     sidblaster_builder_SIDBlasterSID
 * Method:    HardSID_Reset
 * Signature: (B)V
 */
JNIEXPORT void JNICALL Java_sidblaster_1builder_SIDBlasterSID_HardSID_1Reset
  (JNIEnv *, jobject, jbyte);

/*
 * Class:     sidblaster_builder_SIDBlasterSID
 * Method:    HardSID_Delay
 * Signature: (BS)V
 */
JNIEXPORT void JNICALL Java_sidblaster_1builder_SIDBlasterSID_HardSID_1Delay
  (JNIEnv *, jobject, jbyte, jshort);

/*
 * Class:     sidblaster_builder_SIDBlasterSID
 * Method:    HardSID_Flush
 * Signature: (B)V
 */
JNIEXPORT void JNICALL Java_sidblaster_1builder_SIDBlasterSID_HardSID_1Flush
  (JNIEnv *, jobject, jbyte);

#ifdef __cplusplus
}
#endif
#endif
