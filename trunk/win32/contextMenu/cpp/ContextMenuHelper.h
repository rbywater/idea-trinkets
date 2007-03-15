// ContextMenuHelper.h: interface for the CContextMenuHelper class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_CONTEXTMENUHELPER_H__4D06B1C1_AF4F_4A3D_BB3F_C0093A5F656F__INCLUDED_)
#define AFX_CONTEXTMENUHELPER_H__4D06B1C1_AF4F_4A3D_BB3F_C0093A5F656F__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <shlobj.h>

#ifdef ICONTEXTMENU_JNI_EXPORTS
#define ICONTEXTMENU_JNI_API __declspec(dllexport)
#else
#define ICONTEXTMENU_JNI_API __declspec(dllimport)
#endif

#include <vector>
#include <wingdi.h>


typedef std::vector<LPITEMIDLIST> vectorLPITEMIDLIST;

class ICONTEXTMENU_JNI_API CContextMenuHelper  
{
public:
    HRESULT SHGetContextMenu(HWND hWnd, std::vector<LPCTSTR> files, LPVOID *ppv, UINT &cmVersion);
	HMENU SubMenu(HMENU hMenu, UINT wID, IContextMenu *pcm, UINT cmVersion);
	HRESULT GetOwnerDrawBitmap(HMENU hMenu, MENUITEMINFO mii, IContextMenu *pcm, UINT cmVersion, HDC hDC, SIZE &size, HBITMAP &hBitmap);
    CContextMenuHelper();
    virtual ~CContextMenuHelper();

private:
    void FreeItemIDList(std::vector<LPITEMIDLIST> &v, IMalloc *pm);
    LPITEMIDLIST CopyPIDL(LPCITEMIDLIST pidl, IMalloc *pm);
    HRESULT SHBindToParentEx(LPCITEMIDLIST pidl, REFIID riid, LPVOID *ppv, LPCITEMIDLIST *ppidlLast, IShellFolder *psfDesktop, IMalloc *pm);
};

#endif // !defined(AFX_CONTEXTMENUHELPER_H__4D06B1C1_AF4F_4A3D_BB3F_C0093A5F656F__INCLUDED_)
