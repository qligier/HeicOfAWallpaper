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

import java.nio.file.Path;


/**
 * The Java manager around native IDesktopWallpaper methods.
 *
 * @author Quentin Ligier
 * @see <a
 * href="https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nn-shobjidl_core-idesktopwallpaper">IDesktopWallpaper</a>
 * @see <a href="https://github.com/tpn/winsdk-10/blob/master/Include/10.0.14393.0/um/ShObjIdl.idl">ShObjIdl.idl</a>
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
     * The vtable for method GetWallpaper, as found in ShObjIdl.idl: IUnknown has 3 methods, SetWallpaper is the second
     * method in IDesktopWallpaper, so it is at index 4.
     */
    private static final int VTABLE_GETWALLPAPER = 4;

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

    /**
     * Private constructor. Use {@link #create()} to initiate the class.
     *
     * @param pvInstance The COM's pointer.
     */
    private DesktopWallpaperManager(final Pointer pvInstance) {
        super(pvInstance);
    }

    /**
     * Initializes the class and the COM connexion.
     *
     * @return an initialized manager.
     * @throws COMException if the COM initialization fails.
     */
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

    /**
     * Sets the wallpaper image for the given monitor.
     *
     * @param monitorIndex The monitor index.
     * @param jpgWallpaper The path of the wallpaper to set.
     * @throws COMException if a native method call fails.
     */
    public void setJpgWallpaper(final int monitorIndex,
                                final Path jpgWallpaper) throws COMException {

        final WTypes.LPWSTR monitor = new WTypes.LPWSTR(this.getMonitorDevicePathAt(monitorIndex));
        final WTypes.LPWSTR wallpaper = new WTypes.LPWSTR(jpgWallpaper.toAbsolutePath().toString());

        // HRESULT SetWallpaper([in, unique] LPCWSTR monitorID, [in] LPCWSTR wallpaper);
        this.invokeNative(VTABLE_SETWALLPAPER, new Object[]{this.getPointer(), monitor, wallpaper});

        // Instead of using "C:\\1.jpg", use @"C:\1.jpg"?
    }

    /**
     * Gets the path of the current wallpaper image for the given monitor.
     *
     * @param monitorIndex The monitor index.
     * @return The current wallpaper image path.
     * @throws COMException if a native method call fails.
     */
    public String getJpgWallpaper(final int monitorIndex) throws COMException {
        PointerByReference wallpaperPtr = new PointerByReference();
        final WTypes.LPWSTR monitor = new WTypes.LPWSTR(this.getMonitorDevicePathAt(monitorIndex));
        // HRESULT GetWallpaper([in, unique] LPCWSTR monitorID, [out, string] LPWSTR *wallpaper);
        this.invokeNative(VTABLE_GETWALLPAPER, new Object[]{this.getPointer(), monitor, wallpaperPtr});
        return this.extractLpwstr(wallpaperPtr);
    }

    /**
     * Gets the monitor identifier (path) for the given monitor index.
     *
     * @param monitorIndex The monitor index (starting at 0).
     * @return The monitor identifier (path).
     * @throws COMException if a native method call fails.
     */
    public String getMonitorDevicePathAt(final int monitorIndex) throws COMException {
        PointerByReference monitorIdPtr = new PointerByReference();
        // HRESULT GetMonitorDevicePathAt([in] UINT monitorIndex, [out, string] LPWSTR *monitorID);
        this.invokeNative(VTABLE_GETMONITORDEVICEPATHAT, new Object[]{this.getPointer(), monitorIndex, monitorIdPtr});
        return this.extractLpwstr(monitorIdPtr);
    }

    /**
     * Gets the number of monitors.
     * <p>
     * The count retrieved through this method includes monitors that are currently detached but that have an image
     * assigned to them.
     *
     * @return the number of monitors.
     * @throws COMException if a native method call fails.
     */
    public int getMonitorDevicePathCount() throws COMException {
        WinDef.UINTByReference count = new WinDef.UINTByReference();
        // HRESULT GetMonitorDevicePathCount([out] UINT *count);
        this.invokeNative(VTABLE_GETMONITORDEVICEPATHCOUNT, new Object[]{this.getPointer(), count});
        return count.getValue().intValue();
    }

    /**
     * Invokes a IDesktopManager native method by its vtable ID and a list of parameters, and checks the result code.
     * The first parameter shall be the COM's pointer.
     *
     * @param vtableId The method vtable ID.
     * @param parameters The list of call parameters.
     * @throws COMException if the method call failed (i.e. the result code was greater than 0).
     */
    protected void invokeNative(final int vtableId, final Object[] parameters) throws COMException {
        final WinNT.HRESULT result = (WinNT.HRESULT) this._invokeNativeObject(vtableId,
                                                                              parameters,
                                                                              WinNT.HRESULT.class);
        COMUtils.checkRC(result);
    }

    /**
     * Extracts a Java String from a LPWSTR pointer and frees the value (it was not allocated by Java).
     *
     * @param pointer The LPWSTR pointer.
     * @return a Java String or {@code null}.
     * @throws COMException if the pointer has no value.
     */
    protected String extractLpwstr(final PointerByReference pointer) throws COMException {
        if (pointer.getValue() != null) {
            try {
                return pointer.getValue().getWideString(0).strip();
            } finally {
                Ole32.INSTANCE.CoTaskMemFree(pointer.getValue());
            }
        }
        throw new COMException("The LPWSTR pointer has no value");
    }
}
