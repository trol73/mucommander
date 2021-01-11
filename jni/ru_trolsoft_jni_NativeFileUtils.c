#include <stdio.h>

#include "ru_trolsoft_jni_NativeFileUtils.h"

#include <stdbool.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <limits.h>

#define VERSION				2


#define FA_MASK_EXISTS			1
#define FA_MASK_DIRECTORY		2
#define FA_MASK_HIDDEN			4

// https://www.gnu.org/software/libc/manual/html_node/Testing-File-Type.html

static bool is_regular_file(const char *path) {
	struct stat path_stat;
	stat(path, &path_stat);
	return S_ISREG(path_stat.st_mode);
}

static bool is_directory(const char *path) {
	struct stat path_stat;
	stat(path, &path_stat);
	return S_ISDIR(path_stat.st_mode);
}

static bool is_executable_file(const char *path) {
	struct stat path_stat;
	return (stat(path, &path_stat) == 0 && path_stat.st_mode & S_IXUSR);
}

static bool is_hidden_file(const char *path) {
  	char *name = strrchr(path, '/');
  	char folderName[PATH_MAX];
  	if (name && name[0] == '/' && name[1] == 0) {
  		uint len = strlen(path);
  		if (!len || len < 2) {
  			return false;
  		}
  		folderName[len-1] = 0;
  		for (uint i = len-2; i != 0; i--) {
  			folderName[i] = path[i];
  			if (path[i] == '/') {
  				name = &folderName[i];
  				break;
  			}
  		}
  		
  	}  	
  	if (name) {
  	  	if (name[1] == 0) {
  			return false;
  		} else if (name[1] == '.') {
  			return true;
  		} else if (name[2] == 0) {
  			uint len = strlen(path);
  			if (len < 2) {
				return false;
			}
			name = NULL;
			for (uint i = len-2; i != 0; i--) {
				if (path[i] == '/') {
					return path[i+1] == '.';
				}
			}
  		}
  		return false;
  	}
  	return path[0] == '.';
}


JNIEXPORT jint JNICALL Java_ru_trolsoft_jni_NativeFileUtils_getLibraryVersion
	(JNIEnv *env, jclass class) {

	return VERSION;
}

JNIEXPORT jint JNICALL Java_ru_trolsoft_jni_NativeFileUtils_getLocalFileAttributes
  (JNIEnv *env, jclass class, jstring path) {

  	if (path == NULL) {
  		return 0;
  	}
  	const char *pathUtf = (*env)->GetStringUTFChars(env, path, NULL);
  	bool exists = access(pathUtf, F_OK) != -1;
  	bool isDirectory = is_directory(pathUtf);
	bool isHidden = is_hidden_file(pathUtf);
	jint res = 0;
	if (exists) {
		res |= FA_MASK_EXISTS;
	}
	if (isDirectory) {
		res |= FA_MASK_DIRECTORY;
	}
	if (isHidden) {
		res |= FA_MASK_HIDDEN;
	}
	(*env)->ReleaseStringUTFChars(env, path, pathUtf);
  	return res;
}

JNIEXPORT jboolean JNICALL Java_ru_trolsoft_jni_NativeFileUtils_isLocalFileHidden
  (JNIEnv *env, jclass class, jstring path) {

  	if (path == NULL) {
  		return false;
  	}
  	const char *pathUtf = (*env)->GetStringUTFChars(env, path, NULL); 
  	bool result = is_hidden_file(pathUtf);
  	(*env)->ReleaseStringUTFChars(env, path, pathUtf);
  	return result;
}

JNIEXPORT jboolean JNICALL Java_ru_trolsoft_jni_NativeFileUtils_isLocalFileExecutable
  (JNIEnv *env, jclass class, jstring path) {

  	if (path == NULL) {
  		return false;
  	}
  	const char *pathUtf = (*env)->GetStringUTFChars(env, path, NULL); 
  	bool result = is_executable_file(pathUtf);
  	(*env)->ReleaseStringUTFChars(env, path, pathUtf);
  	return result;
}


JNIEXPORT jboolean JNICALL Java_ru_trolsoft_jni_NativeFileUtils_isLocalDirectory
  (JNIEnv *env, jclass class, jstring path) {

  	if (path == NULL) {
  		return false;
  	}
  	const char *pathUtf = (*env)->GetStringUTFChars(env, path, NULL);
	bool result = is_directory(pathUtf);
	(*env)->ReleaseStringUTFChars(env, path, pathUtf);
	return result;
}


