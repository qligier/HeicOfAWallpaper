package ch.qligier.heicofawallpaper.win32;

import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

import java.nio.file.Path;

/**
 * The manager for Shell32.
 *
 * @author Quentin Ligier
 **/
public class Shell32Manager {

    /**
     * This class is not instantiable.
     */
    private Shell32Manager() {
    }

    /**
     * Retrieves the path of the Local AppData folder.
     * <p>
     * The function interface is:
     * <pre>
     * HRESULT SHGetKnownFolderPath(
     *   [in]           REFKNOWNFOLDERID rfid,
     *   [in]           DWORD            dwFlags,
     *   [in, optional] HANDLE           hToken,
     *   [out]          PWSTR            *ppszPath
     * );
     * </pre>
     * <p>
     * [in] rfid: A reference to the {@code KNOWNFOLDERID} that identifies the folder.
     * <p>
     * [in] dwFlags: Flags that specify special retrieval options. This value can be 0; otherwise, one or more of the
     * {@code KNOWN_FOLDER_FLAG} values.
     * <p>
     * [in, optional] hToken: An access token that represents a particular user. If this parameter is {@code null},
     * which is the most common usage, the function requests the known folder for the current user. Request a specific
     * user's folder by passing the hToken of that user. This is typically done in the context of a service that has
     * sufficient privileges to retrieve the token of a given user. That token must be opened with {@code TOKEN_QUERY}
     * and {@code TOKEN_IMPERSONATE} rights. In some cases, you also need to include {@code TOKEN_DUPLICATE}. In
     * addition to passing the user's hToken, the registry hive of that specific user must be mounted. See Access
     * Control for further discussion of access control issues. Assigning the {@code hToken} parameter a value of -1
     * indicates the Default User. This allows clients of SHGetKnownFolderPath to find folder locations (such as the
     * Desktop folder) for the Default User. The Default User user profile is duplicated when any new user account is
     * created, and includes special folders such as Documents and Desktop. Any items added to the Default User folder
     * also appear in any new user account. Note that access to the Default User folders requires administrator
     * privileges.
     * <p>
     * [out] ppszPath: When this method returns, contains the address of a pointer to a null-terminated Unicode string
     * that specifies the path of the known folder. The calling process is responsible for freeing this resource once it
     * is no longer needed by calling {@code CoTaskMemFree}, whether SHGetKnownFolderPath succeeds or not. The returned
     * path does not include a trailing backslash. For example, "C:\Users" is returned rather than "C:\Users\".
     *
     * @return the path of the Local AppData folder for the current user.
     */
    public static Path getLocalAppDataPath() {
        final PointerByReference ppszPath = new PointerByReference();
        final WinNT.HRESULT result = Shell32.INSTANCE.SHGetKnownFolderPath(
            KnownFolders.FOLDERID_LocalAppData,
            0, // No flag
            null, // Current user
            ppszPath
        );
        try {
            COMUtils.checkRC(result);
        } catch (final Exception exception) {
            Ole32.INSTANCE.CoTaskMemFree(ppszPath.getPointer());
            throw exception;
        }
        return Path.of(ppszPath.getValue().getWideString(0));
    }
}
