// ContextMenuHelper.cpp: implementation of the CContextMenuHelper class.
//
//////////////////////////////////////////////////////////////////////

// This is implementation writed with looking into
// sources of WndTabs by Oz Solomonovich.
// Thanks to Oz! :)
// Also thanks to Dmitry Jemerov (yole), who pointed to Oz sources!
//  -- Alexey Efimov (aefimov.box@gmail.com)
// 

#include "stdafx.h"
#include "ContextMenuHelper.h"

// Next macros for LPITEMIDLIST structure
#define NEXTPIDL(pidl) ((LPITEMIDLIST)((((LPBYTE)(pidl)) + (pidl->mkid.cb))))

void CContextMenuHelper::FreeItemIDList(std::vector<LPITEMIDLIST> &v, IMalloc *pm) {
    for (int i = 0; i < v.size(); i++) {
        pm->Free(v[i]);
    }
}

LPITEMIDLIST CContextMenuHelper::CopyPIDL(LPCITEMIDLIST pidl, IMalloc *pm) {
    int cb = pidl->mkid.cb + 2; //add two for terminator

    if (NEXTPIDL(pidl)->mkid.cb != 0) {
        return NULL;            //must be relative (1-length) pidl!
    }

    LPITEMIDLIST pidlNew = (LPITEMIDLIST)pm->Alloc (cb);
    if (pidlNew != NULL) {
        memcpy(pidlNew, pidl, cb);
    }

    return pidlNew;
}

HRESULT CContextMenuHelper::SHGetContextMenu(std::vector<LPCTSTR> files) {
    HRESULT hr;
    IMalloc *pm = NULL;
    IShellFolder *pDesktop = NULL;
    IShellFolder *psf = NULL;
    LPITEMIDLIST pidl = NULL;
    WCHAR fwname[MAX_PATH + 1];

    if (SUCCEEDED(hr = SHGetMalloc(&pm))) {
        if (SUCCEEDED(hr = SHGetDesktopFolder(&pDesktop))) {
            std::vector<LPITEMIDLIST> pidls;
            IShellFolder* psfFolder = NULL;
            for (UINT i = 0; SUCCEEDED(hr) && i < files.size(); i++) {
                LPCTSTR lpszFilePath = files[i];

                ULONG cch;
                ULONG attrs;

                // Convert to Unicode
                memset(fwname, L'\0', (MAX_PATH + 1) * sizeof(WCHAR));
                MultiByteToWideChar(CP_THREAD_ACP, 0, lpszFilePath, -1, fwname, MAX_PATH);
                if (SUCCEEDED(hr = pDesktop->ParseDisplayName(m_hWnd, NULL, fwname, &cch, &pidl, &attrs))) {
                    LPITEMIDLIST pidlItem = NULL;
                    if (SUCCEEDED(hr = SHBindToParentEx((LPCITEMIDLIST)pidl, IID_IShellFolder, (void **)&psf, (LPCITEMIDLIST *) &pidlItem, pDesktop, pm))) {
                        pidls.push_back(CopyPIDL(pidlItem, pm));
                        pm->Free(pidlItem);
                        if (psfFolder == NULL) {
                            // Remember first folder and we wiil show menu for this one
                            // All other folder will be ignored
                            psfFolder = psf;
                        } else {
                            psf->Release();
                        }
                    }
                    pm->Free(pidl);
                }
            }
            if (SUCCEEDED(hr) && psfFolder != NULL) {
                hr = psfFolder->GetUIObjectOf(m_hWnd, pidls.size(), const_cast<LPCITEMIDLIST*>(pidls.begin()), IID_IContextMenu, NULL, (void**)&m_lpcm);
                psfFolder->Release();
            }
            FreeItemIDList(pidls, pm);
            pDesktop->Release();
        }
        pm->Release();
    }

    return hr;
}

// this is workaround function for the Shell API Function SHBindToParent
// SHBindToParent is not available under Win95/98
HRESULT CContextMenuHelper::SHBindToParentEx(LPCITEMIDLIST pidl, REFIID riid, LPVOID *ppv, LPCITEMIDLIST *ppidlLast, IShellFolder *psfDesktop, IMalloc *pm) {
    HRESULT hr = 0;
    if (pidl == NULL || ppv == NULL) {
        return E_POINTER;
    }

    IShellFolder *psfFolder = NULL;
    if (SUCCEEDED(hr == psfDesktop->QueryInterface(riid, (LPVOID*)&psfFolder))) {
        IShellFolder *psfParent = NULL;

        // For each pidl component, bind to folder
        LPITEMIDLIST pidlNext, pidlLast;
        pidlNext = NEXTPIDL(pidl);
        pidlLast = (LPITEMIDLIST)pidl;
        
        while (pidlNext->mkid.cb != 0) {
            
            UINT uSave = pidlNext->mkid.cb;     //stop the chain temporarily
            pidlNext->mkid.cb = 0;              //so we can bind to the next folder 1 deeper
            if (!SUCCEEDED(hr = psfFolder->BindToObject(pidlLast, NULL, riid, (LPVOID*)&psfParent))) {
                return hr;
            }
            pidlNext->mkid.cb = uSave;          //restore the chain

            psfFolder->Release();               //and set up to work with the next-level folder
            psfFolder = psfParent;
            pidlLast = pidlNext;

            pidlNext = NEXTPIDL(pidlNext);      //advance to next pidl
        }

        if (ppidlLast != NULL) {
            *ppidlLast = CopyPIDL((LPCITEMIDLIST)pidlLast, pm);
        }
        *ppv = psfFolder;
    }
    return hr;
}

HMENU CContextMenuHelper::FindMenuItem(HMENU hMenu, UINT index) {
    HMENU hSubMenu = NULL;
    HRESULT hr;

    IContextMenu2* pcm2;
    if (!SUCCEEDED(hr = m_lpcm->QueryInterface(IID_IContextMenu2, (LPVOID*)&pcm2))) {
        pcm2 = NULL;
    }
    MENUITEMINFO mii;
    memset (&mii, 0, sizeof(mii));
    mii.cbSize = sizeof(mii);
    mii.fMask = MIIM_SUBMENU | MIIM_ID | MIIM_TYPE;
    if (::GetMenuItemInfo(hMenu, index, TRUE, &mii)) {
        if (pcm2 != NULL && mii.hSubMenu != NULL) {
            pcm2->HandleMenuMsg(WM_INITMENUPOPUP, (WPARAM)mii.hSubMenu, (LPARAM)mii.wID);
        }
        hSubMenu = mii.hSubMenu;
    }
    if (pcm2 != NULL) {
        pcm2->Release();
    }
    return hSubMenu;
}

int CALLBACK CContextMenuHelper::EnhMetaFileProc(
  HDC hDC,                      // handle to DC
  HANDLETABLE *lpHTable,        // metafile handle table
  CONST ENHMETARECORD *lpEMFR,  // metafile record
  int nObj,                     // count of objects
  LPARAM lpData                 // optional data
) {
    EMREXTTEXTOUTA& emrTOA = *(EMREXTTEXTOUTA*)lpEMFR;
    EMREXTTEXTOUTW& emrTOW = *(EMREXTTEXTOUTW*)lpEMFR;
    CContextMenuHelper* pThis = (CContextMenuHelper*)lpData;

    switch (lpEMFR->iType) {
    case EMR_EXTTEXTOUTA:
        if (emrTOA.emrtext.nChars > 0) {
            pThis->m_iLeftOfText = emrTOA.rclBounds.left;
            lstrcpynA(pThis->m_pBuffer, (LPSTR)((LPBYTE)lpEMFR + emrTOA.emrtext.offString),
                __min(emrTOA.emrtext.nChars, MAX_PATH));
            return FALSE; // stop enum
        }
        break;

    case EMR_EXTTEXTOUTW:
        if (emrTOW.emrtext.nChars > 0) {
            pThis->m_iLeftOfText = emrTOW.rclBounds.left;
            WideCharToMultiByte(CP_ACP, 0, (LPWSTR)((LPBYTE)lpEMFR + emrTOW.emrtext.offString),
                emrTOW.emrtext.nChars, pThis->m_pBuffer, MAX_PATH, NULL, NULL);
            return FALSE; // stop enum
        }
        break;
    }
    
    return TRUE; // go on
}


HBITMAP CContextMenuHelper::InitializeOwnerDrawItem(HMENU hMenu, UINT index, SIZE *lpSize) {
    m_iLeftOfText = 0;
    m_pBuffer = NULL;

    HBITMAP hBitmap = NULL;
    HRESULT hr;

    IContextMenu2* pcm2;
    if (SUCCEEDED(hr = m_lpcm->QueryInterface(IID_IContextMenu2, (LPVOID*)&pcm2))) {
        // get item data
        MENUITEMINFO mii;
        ZeroMemory(&mii, sizeof(MENUITEMINFO));
        mii.cbSize = sizeof(MENUITEMINFO);
        mii.fMask = MIIM_DATA | MIIM_ID;
        if (::GetMenuItemInfo(hMenu, index, TRUE, &mii) && mii.dwItemData != NULL) {
            // get item dimensions
            MEASUREITEMSTRUCT mis;
            ZeroMemory(&mis, sizeof(MEASUREITEMSTRUCT));
            mis.CtlType = ODT_MENU;
            mis.itemData = mii.dwItemData;
            mis.itemID = mii.wID;
            // fake ownerdraw msg
            if (SUCCEEDED(hr = (pcm2->HandleMenuMsg(WM_MEASUREITEM, 0, (LPARAM)&mis)))) {
                if (mis.itemWidth && mis.itemHeight > 0) {
                    HDC hDC = CreateEnhMetaFile(NULL, NULL, NULL, NULL);

                    RECT r = { 0, 0, mis.itemWidth, mis.itemHeight };
                    BOOL bStringFound = FALSE;

                    // fake ownerdraw msg
                    DRAWITEMSTRUCT dis;
                    ZeroMemory(&dis, sizeof(DRAWITEMSTRUCT));
                    dis.CtlType = ODT_MENU;
                    dis.itemID = mii.wID;
                    dis.itemData = mii.dwItemData;
                    dis.itemAction = ODA_DRAWENTIRE;
                    dis.hwndItem = (HWND)hMenu;
                    dis.hDC = hDC;
                    dis.rcItem = r;

                    if (SUCCEEDED(hr = pcm2->HandleMenuMsg(WM_DRAWITEM, 0, (LPARAM)&dis))) {
                        HENHMETAFILE hEMF = CloseEnhMetaFile(hDC);

                        // enum metafile records and search for strings
                        char buf[MAX_PATH] = { 0 };
                        m_pBuffer = buf;
                        m_iLeftOfText = 0;
                        ::EnumEnhMetaFile(NULL, hEMF, EnhMetaFileProc, this, NULL);
                        ::DeleteEnhMetaFile(hEMF);
                        m_pBuffer = NULL;

                        if (buf[0] != 0) {
                            bStringFound = ::ModifyMenu(hMenu, index, MF_BYPOSITION | MF_STRING, mii.wID, buf);
                        }
                    }

                    // Try to get bitmap
                    hDC = ::CreateCompatibleDC(m_hDC);
                    if (hDC != NULL) {
                        // obtain image rect
                        if (bStringFound) {
                            if (m_iLeftOfText > 0) {
                                r.left = (m_iLeftOfText - 16) / 2;
                                r.top = (mis.itemHeight - 16) / 2;
                            } else {
                                r.top = (mis.itemHeight - 16) / 2;
                                r.left = r.top;
                            }
                            r.right = r.left + 16;
                            r.bottom = r.top + 16;
                        }

                        if (lpSize != NULL) {
                            lpSize->cx = r.right - r.left;
                            lpSize->cy = r.bottom - r.top;
                        }
                        hBitmap = ::CreateCompatibleBitmap(m_hDC, r.right - r.left, r.bottom - r.top);
                        if (hBitmap != NULL) {
                            HBITMAP hPrevBitmap = (HBITMAP)::SelectObject(hDC, hBitmap);

                            // draw item on a bitmap
                            COLORREF crBack = ::GetSysColor(COLOR_MENU);

                            ::SetWindowOrgEx(hDC, r.left, r.top, NULL);
                            ::FillRect(hDC, &r, ::CreateSolidBrush(crBack));

                            dis.hDC = hDC;
                            pcm2->HandleMenuMsg(WM_DRAWITEM, 0, (LPARAM)&dis);
                        
                            ::SelectObject(hDC, hPrevBitmap);
                        }
                        ::DeleteDC(hDC);
                    }
                }
            }
        }

        pcm2->Release();
    }

    return hBitmap;
}

HBITMAP CContextMenuHelper::InitializeCheckMarkItem(HMENU hMenu, UINT index) {
    // get item data
    MENUITEMINFO mii;
    ZeroMemory(&mii, sizeof(MENUITEMINFO));
    mii.cbSize = sizeof(MENUITEMINFO);
    mii.fMask = MIIM_CHECKMARKS;
    if (::GetMenuItemInfo(hMenu, index, TRUE, &mii)) {
        HBITMAP hBitmap = NULL;
        if (mii.hbmpChecked != NULL) {
            hBitmap = mii.hbmpChecked;
        } else if (mii.hbmpUnchecked != NULL) {
            hBitmap = mii.hbmpUnchecked;
        }

        if (hBitmap != NULL) {
            // get bitmap size
            // draw item on a bitmap
            COLORREF crBack = RGB(0xFF, 0xFF, 0xFF);
            HDC hMemoryDC = ::CreateCompatibleDC(m_hDC);
            HDC hBitmapDC = ::CreateCompatibleDC(m_hDC);

            HBITMAP hImage = ::CreateCompatibleBitmap(m_hDC, 16, 16);
            HBITMAP old1 = (HBITMAP)::SelectObject(hMemoryDC, hImage);
            HBITMAP old2 = (HBITMAP)::SelectObject(hBitmapDC, hBitmap);

            RECT r = { 0, 0, 16, 16 };
            FillRect(hMemoryDC, &r, ::CreateSolidBrush(crBack));
            // center bitmap
            ::BitBlt(hMemoryDC, 0, 0, 16, 16, hBitmapDC, 0, 0, SRCCOPY);
            ::SelectObject(hMemoryDC, old1);
            ::SelectObject(hBitmapDC, old2);

            ::DeleteDC(hMemoryDC);
            ::DeleteDC(hBitmapDC);

            return hImage;
        }
    }

    return NULL;
}

HRESULT CContextMenuHelper::QueryContextMenu(HMENU hMenu) {
    HRESULT hr;

    IContextMenu2* pcm2;
    if (SUCCEEDED(hr = m_lpcm->QueryInterface(IID_IContextMenu2, (LPVOID*)&pcm2))) {

		hr = pcm2->QueryContextMenu(hMenu, 0, 0, 0x7FFF, CMF_EXPLORE);

		pcm2->Release();
	}
	return hr;
}

HRESULT CContextMenuHelper::GetCommandString(HMENU hMenu, UINT index, char* szHelpText) {
	// Get item description
	ZeroMemory(szHelpText, sizeof(char) * (MAX_PATH + 1));

	UINT nID = ::GetMenuItemID(hMenu, index);

	// try with Unicode first (it seems Explorer does so)
	return m_lpcm->GetCommandString(nID, GCS_HELPTEXTA, NULL, (LPSTR)szHelpText, MAX_PATH);
}

HRESULT CContextMenuHelper::InvokeCommand(HMENU hMenu, HWND hWnd, UINT index) {
    HRESULT hr;

    IContextMenu2* pcm2;
    if (SUCCEEDED(hr = m_lpcm->QueryInterface(IID_IContextMenu2, (LPVOID*)&pcm2))) {
		UINT nID = ::GetMenuItemID(hMenu, index);
		CMINVOKECOMMANDINFO cmi;
		ZeroMemory(&cmi, sizeof(cmi));
		cmi.cbSize       = sizeof(cmi);
		cmi.fMask        = 0;
		cmi.hwnd         = hWnd;
		if (nID == 28) {
			// Fuckin 'Send To' menu...
			// Try to get string and invoke it directly...
            MENUITEMINFO mii;
            char szName[MAX_PATH + 1];
            ZeroMemory(&mii, sizeof(mii));
            ZeroMemory(szName, sizeof(char) * (MAX_PATH + 1));

            mii.cbSize = sizeof(mii);
            mii.fMask = MIIM_SUBMENU | MIIM_ID | MIIM_TYPE | MIIM_DATA;
            mii.dwTypeData = szName;
            mii.cch = sizeof(szName);
			if (::GetMenuItemInfo(hMenu, index, TRUE, &mii) && mii.dwItemData != NULL) {
				cmi.lpVerb = (char*)mii.dwItemData;
			} else {
				cmi.lpVerb = MAKEINTRESOURCE(nID);
			}
		} else {
			cmi.lpVerb   = MAKEINTRESOURCE(nID);
		}
		cmi.lpParameters = NULL;
		cmi.lpDirectory  = NULL;
		cmi.nShow        = SW_SHOWNORMAL;
		cmi.dwHotKey     = 0;
		cmi.hIcon        = NULL;
		hr = m_lpcm->InvokeCommand(&cmi);
	} else {

	}

	return hr;

}


//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CContextMenuHelper::CContextMenuHelper(HWND hWnd, std::vector<LPCTSTR> files) {
    m_hWnd = hWnd;
    m_hDC = ::GetDC(hWnd);
    m_iLeftOfText = 0;
    m_pBuffer = NULL;
    m_lpcm = NULL;
    if (!SUCCEEDED(SHGetContextMenu(files))) {
        m_lpcm = NULL;
    }
}

CContextMenuHelper::~CContextMenuHelper() {
    if (m_lpcm != NULL) {
        m_lpcm->Release();
    }
    if (m_hDC != NULL) {
        ::ReleaseDC(m_hWnd, m_hDC);
    }
}