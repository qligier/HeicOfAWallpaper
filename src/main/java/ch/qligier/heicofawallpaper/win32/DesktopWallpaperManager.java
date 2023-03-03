package ch.qligier.heicofawallpaper.win32;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;


/**
 * The manager for DesktopWallpaper.
 *
 * @author Quentin Ligier
 * @see <a
 * href="https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nn-shobjidl_core-idesktopwallpaper">IDesktopWallpaper</a>
 * @see <a href="https://github.com/tpn/winsdk-10/blob/master/Include/10.0.14393.0/um/ShObjIdl.idl">ShObjIdl.idl</a>
 * <p>
 * <p>
 * https://github.com/matthiasblaesing/JNA-Demos
 * https://stackoverflow.com/questions/44016328/accessing-com-interface-with-jna
 * https://stackoverflow.com/questions/61412613/unable-to-access-the-iactivedesktop-interface-with-jna-no-such-interface-suppor
 **/
public class DesktopWallpaperManager extends Unknown {

    /**
     * The GUID of the IDesktopWallpaper interface, as found in ShObjIdl.idl.
     */
    private static final GUID CLSID_DesktopWallpaper = new GUID("{C2CF3110-460E-4fc1-B9D0-8A1C0C9CC4BD}");

    /**
     * The GUID of the IDesktopWallpaper implementing class, as found in ShObjIdl.idl.
     */
    private static final GUID IID_IDesktopWallpaper = new GUID("{B92B56A9-8B55-4E14-9A89-0199BBB6F93B}");

    /**
     * The vtable for method SetWallpaper, as found in ShObjIdl.idl: IUnknown has 3 methods, SetWallpaper is the first
     * method in IDesktopWallpaper, so it is at index 3.
     */
    private static final int VTABLE_SETWALLPAPER = 3;

    /**
     * The vtable for method GetMonitorDevicePathAt, as found in ShObjIdl.idl: IUnknown has 3 methods,
     * GetMonitorDevicePathAt is the third method in IDesktopWallpaper, so it is at index 5.
     */
    private static final int VTABLE_GETMONITORDEVICEPATHAT = 5;

    /**
     * The vtable for method GetMonitorDevicePathCount, as found in ShObjIdl.idl: IUnknown has 3 methods,
     * GetMonitorDevicePathCount is the fourth method in IDesktopWallpaper, so it is at index 6.
     */
    private static final int VTABLE_GETMONITORDEVICEPATHCOUNT = 6;
    private static final int VTABLE_ID_GET_MONITOR_DEVICE_PATH_AT = 5;

    private DesktopWallpaperManager(final Pointer pvInstance) {
        super(pvInstance);
    }

    public static DesktopWallpaperManager create() throws COMException {
        final PointerByReference p = new PointerByReference();
        final WinNT.HRESULT hr = Ole32.INSTANCE.CoCreateInstance(CLSID_DesktopWallpaper,
                                                                 null,
                                                                 WTypes.CLSCTX_SERVER,
                                                                 IID_IDesktopWallpaper,
                                                                 p);
        COMUtils.checkRC(hr);
        return new DesktopWallpaperManager(p.getValue());
    }

    public List<GraphicsDevice> listScreens() {
        return List.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices());
    }

    public void setJpgWallpaper(final int monitorIndex,
                                final Path jpgWallpaper) {

        final WTypes.LPWSTR monitor = new WTypes.LPWSTR(this.getMonitorDevicePathAt(monitorIndex));
        final WTypes.LPWSTR wallpaper = new WTypes.LPWSTR(jpgWallpaper.toAbsolutePath().toString());

        // HRESULT SetWallpaper([in, unique] LPCWSTR monitorID, [in] LPCWSTR wallpaper);
        int result = this._invokeNativeInt(VTABLE_SETWALLPAPER, new Object[]{this.getPointer(), monitor, wallpaper});
        COMUtils.checkRC(new WinNT.HRESULT(result));

        // Instead of using "C:\\1.jpg", use @"C:\1.jpg"?
    }

    public String getMonitorDevicePathAt(final int monitorIndex) {
        PointerByReference monitorId = new PointerByReference();
        // HRESULT GetMonitorDevicePathAt([in] UINT monitorIndex, [out, string] LPWSTR *monitorID);
        int result = this._invokeNativeInt(VTABLE_GETMONITORDEVICEPATHAT, new Object[]{this.getPointer(),
            monitorIndex, monitorId});
        COMUtils.checkRC(new WinNT.HRESULT(result));
        if (monitorId.getValue() != null) {
            try {
                return monitorId.getValue().getWideString(0);
            } finally {
                Ole32.INSTANCE.CoTaskMemFree(monitorId.getValue());
            }
        } else {
            return null;
        }
    }

    public String GetMonitorDevicePathAt(int monitorIdx) {
        Pointer pResult = Ole32.INSTANCE.CoTaskMemAlloc(0);
        PointerByReference pbr = new PointerByReference(pResult);
        WinNT.HRESULT result = (WinNT.HRESULT) this._invokeNativeObject(VTABLE_ID_GET_MONITOR_DEVICE_PATH_AT,
                                                                        new Object[]{this.getPointer(), monitorIdx, pbr},
                                                                        WinNT.HRESULT.class);
        COMUtils.checkRC(result);
        if (pbr.getValue() != null) {
            try {
                return pbr.getValue().getWideString(0);
            } finally {
                Ole32.INSTANCE.CoTaskMemFree(pbr.getValue());
            }
        } else {
            return null;
        }
    }

    public void test(final Object monitorIndex,
                     final Object monitorId) {
        // HRESULT GetMonitorDevicePathAt([in] UINT monitorIndex, [out, string] LPWSTR *monitorID);
        int result = this._invokeNativeInt(VTABLE_GETMONITORDEVICEPATHAT, new Object[]{this.getPointer(),
            monitorIndex, monitorId});
        System.out.println(result);
        COMUtils.checkRC(new WinNT.HRESULT(result));
    }

    public int getMonitorDevicePathCount() {
        WinDef.UINTByReference count = new WinDef.UINTByReference();
        // HRESULT GetMonitorDevicePathCount([out] UINT *count);
        int result = this._invokeNativeInt(VTABLE_GETMONITORDEVICEPATHCOUNT, new Object[]{this.getPointer(), count});
        COMUtils.checkRC(new WinNT.HRESULT(result));
        return count.getValue().intValue();
    }
}
