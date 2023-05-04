package ch.qligier.heicofawallpaper.win32;

import com.sun.jna.platform.win32.*;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The Java manager around the native User32 methods.
 *
 * @author Quentin Ligier
 **/
public class User32Manager {

    /**
     * This class is not instantiable.
     */
    private User32Manager() {
    }

    /**
     * Retrieves information about the monitors.
     * <p>
     * The function interface is:
     * <pre>
     *     BOOL EnumDisplayMonitors(
     *         [in] HDC             hdc,
     *         [in] LPCRECT         lprcClip,
     *         [in] MONITORENUMPROC lpfnEnum,
     *         [in] LPARAM          dwData
     *     );
     * </pre>
     * <p>
     * {@code [in] hdc}: A handle to a display device context that defines the visible region of interest. If this
     * parameter is {@code null}, the {@code hdcMonitor} parameter passed to the callback function will be {@code null},
     * and the visible region of interest is the virtual screen that encompasses all the displays on the desktop.
     * <p>
     * {@code [in] lprcClip}: A pointer to a {@link WinDef.RECT} structure that specifies a clipping rectangle. The
     * region of interest is the intersection of the clipping rectangle with the visible region specified by
     * {@code hdc}. If {@code hdc} is {@code non-null}, the coordinates of the clipping rectangle are relative to the
     * origin of the {@code hdc}. If {@code hdc} is {@code null}, the coordinates are virtual-screen coordinates. This
     * parameter can be {@code null} if you don't want to clip the region specified by {@code hdc}.
     * <p>
     * {@code [in] lpfnEnum}: A pointer to a {@link WinUser.MONITORENUMPROC} application-defined callback function.
     * <p>
     * {@code [in] dwData}: Application-defined data that EnumDisplayMonitors passes directly to the MonitorEnumProc
     * function.
     *
     * @see <a
     * href="https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-enumdisplaymonitors">EnumDisplayMonitors
     * function (winuser.h)</a>
     * @see <a
     * href="https://github.com/java-native-access/jna/blob/master/contrib/monitordemo/src/com/sun/jna/contrib/demo/MonitorInfoDemo.java">MonitorInfoDemo</a>
     */
    public static void listMonitors() {
        final var result = User32.INSTANCE.EnumDisplayMonitors(null, null, new WinUser.MONITORENUMPROC() {
            @Override
            public int apply(final WinUser.HMONITOR hmonitor,
                             final WinDef.@Nullable HDC hdc,
                             final WinDef.@Nullable RECT rect,
                             final WinDef.LPARAM lparam) {
                WinUser.MONITORINFOEX info = new WinUser.MONITORINFOEX();
                if (!User32.INSTANCE.GetMonitorInfo(hmonitor, info).booleanValue()) {
                    System.out.println("GetMonitorInfo failed:");
                    System.out.println(Kernel32.INSTANCE.GetLastError() + ": " + Kernel32Util.getLastErrorMessage());
                }
                System.out.println("Screen " + info.rcMonitor);
                System.out.println("Work area " + info.rcWork);
                boolean isPrimary = (info.dwFlags & WinUser.MONITORINFOF_PRIMARY) != 0;
                System.out.println("Primary? " + (isPrimary ? "yes" : "no"));
                System.out.println("Device " + new String(info.szDevice).trim());

                WinDef.DWORDByReference pdwNumberOfPhysicalMonitors = new WinDef.DWORDByReference();
                if (!Dxva2.INSTANCE.GetNumberOfPhysicalMonitorsFromHMONITOR(hmonitor,
                                                                            pdwNumberOfPhysicalMonitors).booleanValue()) {
                    System.out.println("GetNumberOfPhysicalMonitorsFromHMONITOR failed:");
                    System.out.println(Kernel32.INSTANCE.GetLastError() + ": " + Kernel32Util.getLastErrorMessage());
                }
                int monitorCount = pdwNumberOfPhysicalMonitors.getValue().intValue();

                System.out.println("HMONITOR is linked to " + monitorCount + " physical monitors");

                var min = new WinDef.DWORDByReference();
                var current = new WinDef.DWORDByReference();
                var max = new WinDef.DWORDByReference();
                Dxva2.INSTANCE.GetMonitorBrightness(hmonitor, min, current, max);

                PhysicalMonitorEnumerationAPI.PHYSICAL_MONITOR[] physMons = new PhysicalMonitorEnumerationAPI.PHYSICAL_MONITOR[monitorCount];
                if (!Dxva2.INSTANCE.GetPhysicalMonitorsFromHMONITOR(hmonitor, monitorCount, physMons).booleanValue()) {
                    System.out.println("GetPhysicalMonitorsFromHMONITOR failed:");
                    System.out.println(Kernel32.INSTANCE.GetLastError() + ": " + Kernel32Util.getLastErrorMessage());
                }
                for (int i = 0; i < monitorCount; i++) {
                    WinNT.HANDLE hPhysicalMonitor = physMons[0].hPhysicalMonitor;
                    System.out.println("Monitor " + i + " - " + new String(physMons[i].szPhysicalMonitorDescription).strip());
                }

                // To continue the enumeration, return 1.
                // To stop the enumeration, return 0.
                return 1;
            }
        }, new WinDef.LPARAM(1));

        if (!result.booleanValue()) {
            System.out.println("EnumDisplayMonitors failed");
        }
    }

}
