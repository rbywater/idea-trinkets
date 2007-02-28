#include "stdafx.h"

#include "org_trinkets_win32_shell_impl_IContextMenuImpl.h"
#include "PidlFromFileSpec.h"
#include <shlobj.h>

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


HRESULT GetUIObjectOfFile(LPCTSTR pszPath, REFIID riid, void **ppv) {
    HRESULT hr;
	PidlVector pidls;
	IShellFolder* psfParent = ShellFolderPidlsFromFileSpec (pszPath, pidls);
	if (psfParent != NULL) {
		hr = psfParent->GetUIObjectOf(NULL, pidls.size(), const_cast<LPCITEMIDLIST*>(pidls.begin()), riid, NULL, ppv);
		psfParent->Release();
	}
	FreePidls(pidls);
	return hr;
}

void FillSubMenu(JNIEnv *env, HMENU hMenu, jobject parent, jclass itemClass, jmethodID cid, jmethodID addChildID) {
    MENUITEMINFO mii;
    char szName[MAX_PATH];
    
    // Browse menu
    int nItems = ::GetMenuItemCount(hMenu);
    for (int iItem = 0; iItem < nItems; iItem++) {
		memset (&mii, 0, sizeof(mii));
		mii.cbSize = sizeof(mii);
		mii.fMask = MIIM_SUBMENU | MIIM_ID | MIIM_TYPE;
        mii.dwTypeData = szName;
        mii.cch = sizeof(szName);
        ::GetMenuItemInfo (hMenu, iItem, TRUE, &mii);

        jstring itemName;
        if (mii.fType & MF_BITMAP) {
            itemName = env->NewStringUTF("${image}");
        } else if (mii.fType & MF_SEPARATOR) {
            itemName = env->NewStringUTF("${separator}");
        } else {
            itemName = env->NewStringUTF(mii.dwTypeData);
        }
        jobject item = env->NewObject(itemClass, cid, mii.wID, itemName);
        /* Free local references */
        env->DeleteLocalRef(itemName);
        
        if (parent != NULL) {
            env->CallVoidMethod(parent, addChildID, item);
        }
        
        // Check sub menu 
        if (mii.hSubMenu) {
            FillSubMenu(env, mii.hSubMenu, item, itemClass, cid, addChildID);
        }
        
        env->DeleteLocalRef(item);
    }
}


JNIEXPORT jobjectArray JNICALL Java_org_trinkets_win32_shell_impl_IContextMenuImpl_getItems0(JNIEnv *env, jobject obj, jstring filePath) {
    // Initialize COM
    if (!SUCCEEDED(OleInitialize(NULL))) {
        JNU_ThrowByName(env, "java/lang/IllegalStateException", "Cannot communicate with the shell because OLE cannot be initialized.");
        return NULL;
    }

    LPCTSTR pszPath = env->GetStringUTFChars(filePath, NULL);
    if (pszPath == NULL) {
        return NULL; // OutOfMemoryError already thrown
    }

    jobjectArray result = NULL;
    IContextMenu* pcm;
    if (SUCCEEDED(GetUIObjectOfFile(pszPath, IID_IContextMenu, (void**)&pcm))) {
		//try to get IContextMenu2 for SendTo
		IContextMenu2* pCM2;
		if (SUCCEEDED(pcm->QueryInterface(IID_IContextMenu2, (LPVOID*)&pCM2)))
		{
			pcm->Release();
			pcm = (IContextMenu*)pCM2;
		}
        HMENU hMenu = CreatePopupMenu();
        if (hMenu) {
            if (SUCCEEDED(pcm->QueryContextMenu(hMenu, 0, 0, 0x7FFF, CMF_EXPLORE))) {
                
                MENUITEMINFO mii;
                char szName[MAX_PATH];
                
                // Browse menu
                int nItems = ::GetMenuItemCount(hMenu);
                jclass menuItemClass = env->FindClass("org/trinkets/win32/shell/IContextMenuItem");
                if (menuItemClass == NULL) {
                    return NULL; // exception thrown
                }
                // Get the method IDs
                jmethodID cid = env->GetMethodID(menuItemClass, "<init>", "(ILjava/lang/String;)V");
                if (cid == NULL) {
                    return NULL; // exception thrown
                }
                jmethodID addChildID = env->GetMethodID(menuItemClass, "addChild", "(Lorg/trinkets/win32/shell/IContextMenuItem;)V");
                if (addChildID == NULL) {
                    return NULL; // exception thrown
                }
                
                // Allocate array 
                result = env->NewObjectArray(nItems, menuItemClass, NULL);
                if (result == NULL) {
                    return NULL; // out of memory error thrown
                }
                for (int iItem = 0; iItem < nItems; iItem++) {
					memset (&mii, 0, sizeof(mii));
					mii.cbSize = sizeof(mii);
					mii.fMask = MIIM_SUBMENU | MIIM_ID | MIIM_TYPE;
                    mii.dwTypeData = szName;
                    mii.cch = sizeof(szName);
                    ::GetMenuItemInfo (hMenu, iItem, TRUE, &mii);

                    jstring itemName;
                    if (mii.fType & MF_BITMAP) {
                        itemName = env->NewStringUTF("${image}");
                    } else if (mii.fType & MF_SEPARATOR) {
                        itemName = env->NewStringUTF("${separator}");
                    } else {
                        itemName = env->NewStringUTF(mii.dwTypeData);
                    }
                    jobject item = env->NewObject(menuItemClass, cid, mii.wID, itemName);
                    /* Free local references */
                    env->DeleteLocalRef(itemName);
                    
                    // Check sub menu 
                    if (mii.hSubMenu) {
                        FillSubMenu(env, mii.hSubMenu, item, menuItemClass, cid, addChildID);
                    }
                    
                    env->SetObjectArrayElement(result, iItem, item);
                    env->DeleteLocalRef(item);
                }
                /* Free local references */
                env->DeleteLocalRef(menuItemClass);
            }
            DestroyMenu(hMenu);
        }
        pcm->Release();
    }
    // Free
    env->ReleaseStringUTFChars(filePath, pszPath);
    OleUninitialize();
    
    return result;
};

JNIEXPORT void JNICALL Java_org_trinkets_win32_shell_impl_IContextMenuImpl_invokeItem0(JNIEnv *env, jobject obj, jstring filePath, jint item) {
    // Initialize COM
    if (!SUCCEEDED(OleInitialize(NULL))) {
        JNU_ThrowByName(env, "java/lang/IllegalStateException", "Cannot communicate with the shell because OLE cannot be initialized.");
        return;
    }

    LPCTSTR pszPath = env->GetStringUTFChars(filePath, NULL);
    if (pszPath == NULL) {
        return; /* OutOfMemoryError already thrown */
    }
    IContextMenu* pcm;
    if (SUCCEEDED(GetUIObjectOfFile(pszPath, IID_IContextMenu, (void**)&pcm))) {
		//try to get IContextMenu2 for SendTo
		IContextMenu2* pCM2;
		if (SUCCEEDED(pcm->QueryInterface(IID_IContextMenu2, (LPVOID*)&pCM2)))
		{
			pcm->Release();
			pcm = (IContextMenu*)pCM2;
		}
        HMENU hMenu = CreatePopupMenu();
        if (hMenu) {
            if (SUCCEEDED(pcm->QueryContextMenu(hMenu, 0, 0, 0x7FFF, CMF_EXPLORE))) {
                
                CMINVOKECOMMANDINFO info = { 0 };
                info.cbSize = sizeof(info);
                info.hwnd = NULL;
                info.nShow = SW_SHOWNORMAL;
                info.lpVerb = MAKEINTRESOURCE(item);
                
                pcm->InvokeCommand(&info);
            }
            DestroyMenu(hMenu);
        }
        pcm->Release();
    }
    // Free
    env->ReleaseStringUTFChars(filePath, pszPath);
    OleUninitialize();
};
