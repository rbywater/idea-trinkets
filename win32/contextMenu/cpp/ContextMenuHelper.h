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
    IContextMenu *m_lpcm;
    HDC m_hDC;

    HMENU FindMenuItem(HMENU hMenu, UINT wID);
    HBITMAP InitializeOwnerDrawItem(HMENU hMenu, UINT index, SIZE *lpSize);
    HBITMAP InitializeCheckMarkItem(HMENU hMenu, UINT index);
    CContextMenuHelper(HWND hWnd, std::vector<LPCTSTR> files);
    virtual ~CContextMenuHelper();

private:
    HWND m_hWnd;
   	int m_iLeftOfText;
	LPSTR m_pBuffer;

    void FreeItemIDList(std::vector<LPITEMIDLIST> &v, IMalloc *pm);
    LPITEMIDLIST CopyPIDL(LPCITEMIDLIST pidl, IMalloc *pm);
    HRESULT SHGetContextMenu(std::vector<LPCTSTR> files);
    HRESULT SHBindToParentEx(LPCITEMIDLIST pidl, REFIID riid, LPVOID *ppv, LPCITEMIDLIST *ppidlLast, IShellFolder *psfDesktop, IMalloc *pm);
	static int CALLBACK EnhMetaFileProc(
	  HDC hDC,                      // handle to DC
	  HANDLETABLE *lpHTable,        // metafile handle table
	  CONST ENHMETARECORD *lpEMFR,  // metafile record
	  int nObj,                     // count of objects
	  LPARAM lpData                 // optional data
	);
};

#endif // !defined(AFX_CONTEXTMENUHELPER_H__4D06B1C1_AF4F_4A3D_BB3F_C0093A5F656F__INCLUDED_)
