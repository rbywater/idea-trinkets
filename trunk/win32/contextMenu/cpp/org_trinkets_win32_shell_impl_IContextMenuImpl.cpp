#include "stdafx.h"

#include "org_trinkets_win32_shell_impl_IContextMenuImpl.h"
#include "ContextMenuHelper.h"
#include "jawt_md.h"
#include <shlobj.h>
#include <vector>

/**
 * Utility from http://www-128.ibm.com/developerworks/java/library/j-jninls/jninls.html
 */
char* jstringToWindows( JNIEnv * env, jstring jstr )
{
  int length = env->GetStringLength( jstr );
  const jchar* jcstr = env->GetStringChars( jstr, 0 );
  char* rtn = (char*)malloc( length*2+1 );
  int size = 0;
  size = WideCharToMultiByte( CP_ACP, 0, (LPCWSTR)jcstr, length, rtn,
                           (length*2+1), NULL, NULL );
  if( size <= 0 )
    return NULL;
  env->ReleaseStringChars( jstr, jcstr );
  rtn[size] = 0;
  return rtn;
}

/**
 * Utility from http://www-128.ibm.com/developerworks/java/library/j-jninls/jninls.html
 */
jstring WindowsTojstring( JNIEnv* env, char* str )
{
  jstring rtn = 0;
  int slen = strlen(str);
  wchar_t* buffer = 0;
  if( slen == 0 )
    rtn = env->NewStringUTF( str ); //UTF ok since empty string
  else
  {
    int length = 
      MultiByteToWideChar( CP_ACP, 0, (LPCSTR)str, slen, NULL, 0 );
    buffer = (wchar_t*)malloc( length*2 + 1 );
    if( MultiByteToWideChar( CP_ACP, 0, (LPCSTR)str, slen, 
        (LPWSTR)buffer, length ) >0 )
      rtn = env->NewString( (jchar*)buffer, length );
  }
  if( buffer )
   free( buffer );
  return rtn;
}

/**
 * Utility from http://java.sun.com/docs/books/jni/html/exceptions.html#26050
 */
void JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg) {
    jclass cls = env->FindClass(name);
    /* if cls is NULL, an exception has already been thrown */
    if (cls != NULL) {
        env->ThrowNew(cls, msg);
    }
    /* free the local ref */
    env->DeleteLocalRef(cls);
}

BOOL JNU_StringArrayToVector(JNIEnv *env, jobjectArray strings, std::vector<LPCTSTR> *files) {
    
    UINT n = env->GetArrayLength(strings);
    for (UINT i = 0; i < n; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(strings, i);
        LPCTSTR psz = jstringToWindows(env, str);
        if (psz == NULL) {
            return FALSE; // OutOfMemoryError already thrown
        }
        files->push_back(psz);
    }
	return TRUE;
}

HWND GetAWTWindow(JNIEnv *env, jobject component) {
    HWND hWnd = NULL;

    // Obtain drawing surface
    JAWT awt;
    
    // Get the AWT
    awt.version = JAWT_VERSION_1_4;
    if (JAWT_GetAWT(env, &awt) != JNI_FALSE) {
        // Get the drawing surface
        JAWT_DrawingSurface* ds = awt.GetDrawingSurface(env, component);
        if (ds != NULL) {
            // Lock the drawing surface
            jint lock = ds->Lock(ds);
            if ((lock & JAWT_LOCK_ERROR) == 0) {
                // Get the drawing surface info
                JAWT_DrawingSurfaceInfo* dsi = ds->GetDrawingSurfaceInfo(ds);
                if (dsi != NULL) {
                    // Get the platform-specific drawing info
                    JAWT_Win32DrawingSurfaceInfo* dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
                    if (dsi_win != NULL) {
                        hWnd = dsi_win->hwnd;
                        ds->FreeDrawingSurfaceInfo(dsi);
                    }
                }
                ds->Unlock(ds);
            }
            awt.FreeDrawingSurface(ds);
        }
    }

    return hWnd;
}

jintArray JNU_BitmapToJNIArray(JNIEnv *env, HDC hDC, HBITMAP hBitmap, UINT width, UINT height) {
    jintArray pixelArray = NULL;

    if (hBitmap != NULL ) {
        if (width > 0 && height > 0) {
            // Get bitmap bits
            BITMAPINFO bmi;
            memset(&bmi, 0, sizeof(BITMAPINFO));
            bmi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
            bmi.bmiHeader.biWidth = width;
            bmi.bmiHeader.biHeight = -height;
            bmi.bmiHeader.biPlanes = 1;
            bmi.bmiHeader.biBitCount = 32;
            bmi.bmiHeader.biCompression = BI_RGB;

            jintArray localPixelArray = (jintArray)env->NewIntArray(width * height);
            if (localPixelArray == NULL) {
                return NULL; // Exception thrown
            }
            pixelArray = (jintArray)env->NewGlobalRef(localPixelArray);
            if (pixelArray == NULL) {
                return NULL; // Exception thrown
            }
            env->DeleteLocalRef(localPixelArray); 
            localPixelArray = NULL;

            jboolean isCopy;
            jint *pixels = env->GetIntArrayElements(pixelArray, &isCopy);
            ::GetDIBits(hDC, hBitmap, 0, height, (LPVOID)pixels, &bmi, DIB_RGB_COLORS);

            // Delete bitmap
            ::DeleteObject(hBitmap);

            env->ReleaseIntArrayElements(pixelArray, pixels, 0);
        }
    }

    return pixelArray;
}

jintArray FixOwnerDrawItem(JNIEnv *env, CContextMenuHelper *cm, HMENU hMenu, UINT index, SIZE *imageSize) {
    HBITMAP hBitmap = cm->InitializeOwnerDrawItem(hMenu, index, imageSize);
    return JNU_BitmapToJNIArray(env, cm->m_hDC, hBitmap, imageSize->cx, imageSize->cy);
}

jintArray FixCheckMarkItem(JNIEnv *env, CContextMenuHelper *cm, HMENU hMenu, UINT index) {
    HBITMAP hBitmap = cm->InitializeCheckMarkItem(hMenu, index);
    return JNU_BitmapToJNIArray(env, cm->m_hDC, hBitmap, 16, 16);
}


JNIEXPORT jobjectArray JNICALL Java_org_trinkets_win32_shell_impl_IContextMenuImpl_getItems0(JNIEnv *env, jobject obj, jobject awtOwner, jobjectArray filePaths, jintArray menuPath) {
    // Initialize COM
    if (!SUCCEEDED(OleInitialize(NULL))) {
        JNU_ThrowByName(env, "java/lang/IllegalStateException", "Cannot communicate with the shell because OLE cannot be initialized.");
        return NULL;
    }
    

	std::vector<LPCTSTR> files;
	if (!JNU_StringArrayToVector(env, filePaths, &files)) {
		return NULL; // Exception
	}

    UINT nMenuPaths = env->GetArrayLength(menuPath);

    jobjectArray result = NULL;
    HWND hWnd = GetAWTWindow(env, awtOwner);

    CContextMenuHelper *cm = new CContextMenuHelper(hWnd, files);
    if (cm->m_lpcm != NULL) {
        HMENU hMenu = CreatePopupMenu();
        if (hMenu != NULL) {
            if (SUCCEEDED(cm->QueryContextMenu(hMenu))) {
    
                // Browse for parent
                jint *menuPathElements = env->GetIntArrayElements(menuPath, 0);
                HMENU hParent = hMenu;
                for (UINT i = 0; hParent != NULL && i < nMenuPaths; i++) {
                    hParent = cm->FindMenuItem(hParent, menuPathElements[i]);
                }
                env->ReleaseIntArrayElements(menuPath, menuPathElements, 0);

                if (hParent != NULL) {

                    // Browse menu
                    MENUITEMINFO mii;

                    int nItems = ::GetMenuItemCount(hParent);
                    char szName[MAX_PATH + 1];
                    char szHelpText[MAX_PATH + 1];

                    jclass menuItemClass = env->FindClass("org/trinkets/win32/shell/IContextMenuItem");
                    if (menuItemClass == NULL) {
                        return NULL; // exception thrown
                    }
                    jclass idbClass = env->FindClass("java/awt/image/DataBufferInt");
                    if (idbClass == NULL) {
                        return NULL; // exception thrown
                    }
                    // Get the method IDs
                    jmethodID cid = env->GetMethodID(menuItemClass, "<init>", "(ILjava/lang/String;Ljava/lang/String;ZLjava/awt/image/DataBuffer;II)V");
                    if (cid == NULL) {
                        return NULL; // exception thrown
                    }                
                    // Get the method IDs
                    jmethodID idbCid = env->GetMethodID(idbClass, "<init>", "([II)V");
                    if (idbCid == NULL) {
                        return NULL; // exception thrown
                    }                
                    
                    // Allocate array 
                    result = env->NewObjectArray(nItems, menuItemClass, NULL);
                    if (result == NULL) {
                        return NULL; // out of memory error thrown
                    }
                    for (int iItem = 0; iItem < nItems; iItem++) {
                        SIZE imageSize = {0, 0};

                        jintArray pixelArray = FixOwnerDrawItem(env, cm, hParent, iItem, &imageSize);
                        if (pixelArray == NULL) {
                            pixelArray = FixCheckMarkItem(env, cm, hParent, iItem);
                            if (pixelArray != NULL) {
                                imageSize.cx = 16;
                                imageSize.cy = 16;
                            }
                        }

                        ZeroMemory(&mii, sizeof(mii));
                        ZeroMemory(szName, sizeof(char) * (MAX_PATH + 1));

                        mii.cbSize = sizeof(mii);
                        mii.fMask = MIIM_SUBMENU | MIIM_ID | MIIM_TYPE | MIIM_DATA;
                        mii.dwTypeData = szName;
                        mii.cch = sizeof(szName);

                        ::GetMenuItemInfo(hParent, iItem, TRUE, &mii);

                        jstring itemText;
                        jstring itemDescription;
                        jobject imageDataBuffer = NULL;

                        if (mii.fType & MF_SEPARATOR) {
                            itemText = WindowsTojstring(env, "${separator}");
                            itemDescription = WindowsTojstring(env, "");
                        } else {
                            if (mii.fType & MF_BITMAP) {
                                itemText = WindowsTojstring(env, "${bitmap}");
                            } else if (mii.fType & MF_OWNERDRAW) {
                                itemText = WindowsTojstring(env, "${ownerdraw}");
                            } else if (mii.dwTypeData != NULL) {
                                itemText = WindowsTojstring(env, mii.dwTypeData);
                            } else {
                                itemText = WindowsTojstring(env, "${unknown}");
                            }

                            // Get item description
                            ZeroMemory(szHelpText, sizeof(char) * (MAX_PATH + 1));

                            // try with Unicode first (it seems Explorer does so)
                            HRESULT hr = cm->m_lpcm->GetCommandString(mii.wID, GCS_HELPTEXTA, NULL, (LPSTR)szHelpText, MAX_PATH);
                            if (szHelpText != NULL) {
                                // buffer was used
                                itemDescription = WindowsTojstring(env, szHelpText);
                            } else {
                                itemDescription = WindowsTojstring(env, "");
                            }
                        } 


                        // Create buffered image
                        if (pixelArray != NULL && imageSize.cx > 0 && imageSize.cy > 0) {
                            imageDataBuffer = env->NewObject(idbClass, idbCid, pixelArray, imageSize.cx * imageSize.cy);
                            if (imageDataBuffer == NULL) {
                                return NULL; // Exception thrown
                            }
                        }

                        // Create new menu item
                        jobject item = env->NewObject(menuItemClass, cid, iItem, itemText, itemDescription, mii.hSubMenu != NULL, imageDataBuffer, imageSize.cx, imageSize.cy);
                        if (item == NULL) {
                            return NULL; // Exception thrown
                        }

                        // Free local references
                        env->DeleteLocalRef(itemText);
                        env->DeleteLocalRef(itemDescription);
                        env->SetObjectArrayElement(result, iItem, item);
                        env->DeleteLocalRef(item);
                        if (imageDataBuffer != NULL) {
                            env->DeleteLocalRef(imageDataBuffer);
                        }
                    }
                    // Free local references
                    env->DeleteLocalRef(menuItemClass);
                    env->DeleteLocalRef(idbClass);
                }
            }
            DestroyMenu(hMenu);
        }
    }

    delete cm;
    // Free 
    for (UINT i = 0; i < files.size(); i++) {
        free((LPVOID)files[i]);
    }
    OleUninitialize();
    
    return result;
};

JNIEXPORT void JNICALL Java_org_trinkets_win32_shell_impl_IContextMenuImpl_invokeItem0(JNIEnv *env, jobject obj, jobject awtOwner, jobjectArray filePaths, jintArray menuPath, jint item) {
    // Initialize COM
    if (!SUCCEEDED(OleInitialize(NULL))) {
        JNU_ThrowByName(env, "java/lang/IllegalStateException", "Cannot communicate with the shell because OLE cannot be initialized.");
        return;
    }

    std::vector<LPCTSTR> files;
	if (!JNU_StringArrayToVector(env, filePaths, &files)) {
		return; // Exception
	}

    UINT nMenuPaths = env->GetArrayLength(menuPath);

    HWND hWnd = GetAWTWindow(env, awtOwner);
    CContextMenuHelper *cm = new CContextMenuHelper(hWnd, files);
    if (cm->m_lpcm != NULL) {
        HMENU hMenu = CreatePopupMenu();
        if (hMenu != NULL) {
            if (SUCCEEDED(cm->QueryContextMenu(hMenu))) {


                // Browse for parent
                jint *menuPathElements = env->GetIntArrayElements(menuPath, 0);
                HMENU hParent = hMenu;
                for (UINT i = 0; hParent != NULL && i < nMenuPaths; i++) {
                    hParent = cm->FindMenuItem(hParent, menuPathElements[i]);
                }
                env->ReleaseIntArrayElements(menuPath, menuPathElements, 0);

                if (hParent != NULL) {
                    SIZE imageSize = {0, 0};

                    jintArray pixelArray = FixOwnerDrawItem(env, cm, hParent, item, &imageSize);
                    if (pixelArray == NULL) {
                        pixelArray = FixCheckMarkItem(env, cm, hParent, item);
                        if (pixelArray != NULL) {
                            imageSize.cx = 16;
                            imageSize.cy = 16;
                        }
                    }

					cm->InvokeCommand(hParent, hWnd, item);
                }
            }
            DestroyMenu(hMenu);
        }
    }
    delete cm;
    // Free
    for (UINT i = 0; i < files.size(); i++) {
        free((LPVOID)files[i]);
    }
    OleUninitialize();
};
