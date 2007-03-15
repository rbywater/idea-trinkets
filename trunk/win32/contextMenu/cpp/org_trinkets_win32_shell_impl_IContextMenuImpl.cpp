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

JNIEXPORT jobjectArray JNICALL Java_org_trinkets_win32_shell_impl_IContextMenuImpl_getItems0(JNIEnv *env, jobject obj, jobject awtOwner, jobjectArray filePaths, jintArray menuPath) {
	// Initialize COM
	if (!SUCCEEDED(OleInitialize(NULL))) {
		JNU_ThrowByName(env, "java/lang/IllegalStateException", "Cannot communicate with the shell because OLE cannot be initialized.");
		return NULL;
	}
	
    std::vector<LPCTSTR> files;
	UINT nFilePaths = env->GetArrayLength(filePaths);
	for (UINT i = 0; i < nFilePaths; i++) {
		jstring filePath = (jstring)env->GetObjectArrayElement(filePaths, i);
		LPCTSTR pszPath = jstringToWindows(env, filePath);
		if (pszPath == NULL) {
			return NULL; // OutOfMemoryError already thrown
		}
		files.push_back(pszPath);
	}

	UINT nMenuPaths = env->GetArrayLength(menuPath);

	jobjectArray result = NULL;
	CContextMenuHelper cmHelper;
	IContextMenu* pcm;
	UINT cmVersion = 1;

	HWND hWnd = GetAWTWindow(env, awtOwner);
	if (SUCCEEDED(cmHelper.SHGetContextMenu(hWnd, files, (void**)&pcm, cmVersion))) {
		HMENU hMenu = CreatePopupMenu();
		if (hMenu != NULL) {
			if (SUCCEEDED(pcm->QueryContextMenu(hMenu, 0, 0, 0xFFFFFFFF, CMF_EXPLORE))) {
    
				// Browse for parent
				jint *menuPathElements = env->GetIntArrayElements(menuPath, 0);
				HMENU hParent = hMenu;
				for (UINT i = 0; hParent != NULL && i < nMenuPaths; i++) {
					hParent = cmHelper.SubMenu(hParent, menuPathElements[i], pcm, cmVersion);
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
					
					HDC hDC = NULL;
					if (hWnd != NULL && ::IsWindowVisible(hWnd)) {
						hDC = ::GetDC(hWnd);
					}

					// Allocate array 
					result = env->NewObjectArray(nItems, menuItemClass, NULL);
					if (result == NULL) {
						return NULL; // out of memory error thrown
					}
					for (int iItem = 0; iItem < nItems; iItem++) {
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
						SIZE imageSize = {0, 0};

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

							if (hDC != NULL) {
								HBITMAP hBitmap = NULL;
								HDC hMemoryDC = ::CreateCompatibleDC(hDC);

								if (SUCCEEDED(cmHelper.GetOwnerDrawBitmap(hParent, mii, pcm, cmVersion, hMemoryDC, imageSize, hBitmap))) {
									if (hBitmap != NULL ) {
										if (imageSize.cx > 0 && imageSize.cy > 0) {
											// Get bitmap bits
											BITMAPINFO bmi;
											memset(&bmi, 0, sizeof(BITMAPINFO));
											bmi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
											bmi.bmiHeader.biWidth = imageSize.cx;
											bmi.bmiHeader.biHeight = -imageSize.cy;
											bmi.bmiHeader.biPlanes = 1;
											bmi.bmiHeader.biBitCount = 32;
											bmi.bmiHeader.biCompression = BI_RGB;

											jintArray localPixelArray = (jintArray)env->NewIntArray(imageSize.cx * imageSize.cy);
											if (localPixelArray == NULL) {
												return NULL; // Exception thrown
											}
											jintArray pixelArray = (jintArray)env->NewGlobalRef(localPixelArray);
											if (pixelArray == NULL) {
												return NULL; // Exception thrown
											}
											env->DeleteLocalRef(localPixelArray); 
											localPixelArray = NULL;

											jboolean isCopy;
											jint *pixels = env->GetIntArrayElements(pixelArray, &isCopy);
											::GetDIBits(hMemoryDC, hBitmap, 0, imageSize.cy, (LPVOID)pixels, &bmi, DIB_RGB_COLORS);

											// Delete bitmap
											::DeleteObject(hBitmap);

											// Create buffered image
											imageDataBuffer = env->NewObject(idbClass, idbCid, pixelArray, imageSize.cx * imageSize.cy);
											if (imageDataBuffer == NULL) {
												return NULL; // Exception thrown
											}

											env->ReleaseIntArrayElements(pixelArray, pixels, 0);
										}
									}
								}

								::DeleteDC(hMemoryDC);
							}

							// Get item description
							ZeroMemory(szHelpText, sizeof(char) * (MAX_PATH + 1));

							// try with Unicode first (it seems Explorer does so)
							HRESULT hr = pcm->GetCommandString(mii.wID, GCS_HELPTEXTA, NULL, (LPSTR)szHelpText, MAX_PATH);
							if (szHelpText != NULL) {
								// buffer was used
								itemDescription = WindowsTojstring(env, szHelpText);
							} else {
								itemDescription = WindowsTojstring(env, "");
							}
						} 


						// Create new menu item
						jobject item = env->NewObject(menuItemClass, cid, mii.wID, itemText, itemDescription, mii.hSubMenu != NULL, imageDataBuffer, imageSize.cx, imageSize.cy);
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
					// Delete context
					if (hDC != NULL) {
						::ReleaseDC(hWnd, hDC);
					}
				}
			}
			DestroyMenu(hMenu);
		}
		pcm->Release();
	}

	// Free 
	for (i = 0; i < files.size(); i++) {
		free((LPVOID)files[i]);
	}
	OleUninitialize();
    
    return result;
};

JNIEXPORT void JNICALL Java_org_trinkets_win32_shell_impl_IContextMenuImpl_invokeItem0(JNIEnv *env, jobject obj, jobjectArray filePaths, jintArray menuPath, jint item) {
    // Initialize COM
    if (!SUCCEEDED(OleInitialize(NULL))) {
        JNU_ThrowByName(env, "java/lang/IllegalStateException", "Cannot communicate with the shell because OLE cannot be initialized.");
        return;
    }

    std::vector<LPCTSTR> files;
	UINT nFilePaths = env->GetArrayLength(filePaths);
	for (UINT i = 0; i < nFilePaths; i++) {
		jstring filePath = (jstring)env->GetObjectArrayElement(filePaths, i);
		LPCTSTR pszPath = jstringToWindows(env, filePath);
		if (pszPath == NULL) {
			return; // OutOfMemoryError already thrown
		}
		files.push_back(pszPath);
	}

	UINT nMenuPaths = env->GetArrayLength(menuPath);

	CContextMenuHelper cmHelper;
	UINT cmVersion;
	IContextMenu* pcm;
	if (SUCCEEDED(cmHelper.SHGetContextMenu(NULL, files, (void**)&pcm, cmVersion))) {
		HMENU hMenu = CreatePopupMenu();
		if (hMenu != NULL) {
			if (SUCCEEDED(pcm->QueryContextMenu(hMenu, 0, 0, 0xFFFFFFFF, CMF_EXPLORE))) {

				// Browse for parent
				jint *menuPathElements = env->GetIntArrayElements(menuPath, 0);
				HMENU hParent = hMenu;
				for (UINT i = 0; hParent != NULL && i < nMenuPaths; i++) {
					hParent = cmHelper.SubMenu(hParent, menuPathElements[i], pcm, cmVersion);
				}
				env->ReleaseIntArrayElements(menuPath, menuPathElements, 0);

				if (hParent != NULL) {
					CMINVOKECOMMANDINFO info = { 0 };
					info.cbSize = sizeof(info);
					info.hwnd = NULL;
					info.nShow = SW_SHOWNORMAL;
					info.lpVerb = MAKEINTRESOURCE(item);

					pcm->InvokeCommand(&info);
				}
			}
			DestroyMenu(hMenu);
		}
		pcm->Release();
	}
	// Free
	for (i = 0; i < files.size(); i++) {
		free((LPVOID)files[i]);
	}
	OleUninitialize();
};
