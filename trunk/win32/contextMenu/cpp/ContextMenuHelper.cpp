// ContextMenuHelper.cpp: implementation of the CContextMenuHelper class.
//
//////////////////////////////////////////////////////////////////////

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

HRESULT CContextMenuHelper::SHGetContextMenu(HWND hWnd, std::vector<LPCTSTR> files, LPVOID* ppv, UINT &cmVersion) {
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
                if (SUCCEEDED(hr = pDesktop->ParseDisplayName(hWnd, NULL, fwname, &cch, &pidl, &attrs))) {
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
                IContextMenu *pcm;
                if (SUCCEEDED(psfFolder->GetUIObjectOf(hWnd, pidls.size(), const_cast<LPCITEMIDLIST*>(pidls.begin()), IID_IContextMenu, NULL, (void**)&pcm))) {
                    cmVersion = 1;
                    IContextMenu3 *pcm3;
                    if (SUCCEEDED(hr = pcm->QueryInterface(IID_IContextMenu3, (void**)&pcm3))) {
                        pcm->Release();
                        pcm = pcm3;
                        cmVersion = 3;
                    } else {
                        IContextMenu2 *pcm2;
                        if (SUCCEEDED(hr = pcm->QueryInterface(IID_IContextMenu2, (void**)&pcm2))) {
                            pcm->Release();
                            pcm = pcm2;
                            cmVersion = 2;
                        }
                    }
                    *ppv = (VOID*)pcm;
                }
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

HMENU CContextMenuHelper::SubMenu(HMENU hMenu, UINT wID, IContextMenu *pcm, UINT cmVersion) {
	MENUITEMINFO mii;
	int nItems = ::GetMenuItemCount(hMenu);
	for (UINT i = 0; i < nItems; i++) {
        memset (&mii, 0, sizeof(mii));
        mii.cbSize = sizeof(mii);
        mii.fMask = MIIM_SUBMENU | MIIM_ID | MIIM_TYPE;

        ::GetMenuItemInfo(hMenu, i, TRUE, &mii);

		if (mii.wID == wID && mii.hSubMenu != NULL) {
            if (cmVersion >= 2) {
                ((IContextMenu2*)pcm)->HandleMenuMsg(WM_INITMENUPOPUP, (WPARAM)mii.hSubMenu, (LPARAM)mii.wID);
            }
			return mii.hSubMenu;
		}
	}
	return NULL;
}


HRESULT CContextMenuHelper::GetOwnerDrawBitmap(HMENU hMenu, MENUITEMINFO mii, IContextMenu *pcm, UINT cmVersion, HDC hDC, SIZE &size, HBITMAP &hBitmap) {
	HRESULT hr;

	if (cmVersion >= 2) {
		// get item dimensions
		MEASUREITEMSTRUCT mis;
		ZeroMemory(&mis, sizeof(MEASUREITEMSTRUCT));
		mis.CtlType = ODT_MENU;
		mis.itemData = mii.dwItemData;
		mis.itemID = mii.wID;
		// fake ownerdraw msg
		if (SUCCEEDED(hr = ((IContextMenu2*)pcm)->HandleMenuMsg(WM_MEASUREITEM, 0, (LPARAM)&mis))) {
			// draw item on a bitmap
			hBitmap = ::CreateCompatibleBitmap(hDC, mis.itemWidth, mis.itemHeight);
			if (hBitmap != NULL) {
				HBITMAP hPrevBitmap = (HBITMAP) SelectObject(hDC, hBitmap);

				RECT r = { 0, 0, mis.itemWidth, mis.itemHeight };
				COLORREF crBack = ::GetSysColor(COLOR_MENU);
				::FillRect(hDC, &r, ::CreateSolidBrush(crBack));
				::SetWindowOrgEx(hDC, 0, 0, NULL);

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
				if (SUCCEEDED(hr = ((IContextMenu2*)pcm)->HandleMenuMsg(WM_DRAWITEM, 0, (LPARAM)&dis))) {
					size.cx = mis.itemWidth;
					size.cy = mis.itemHeight;
  					SelectObject(hDC, hPrevBitmap);
				}
			}
		}
	}

	return hr;
}


//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CContextMenuHelper::CContextMenuHelper() {
}

CContextMenuHelper::~CContextMenuHelper() {
}