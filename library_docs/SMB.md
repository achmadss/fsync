# SMB — jcifs-ng

**Package:** `jcifs.smb`, `jcifs.context`, `jcifs.config`
**Library:** `eu.agno3.jcifs:jcifs-ng:2.1.10`

API extracted from the sources jar.

---

## Table of Contents

- [Setup](#setup)
  - [PropertyConfiguration](#propertyconfiguration)
  - [BaseContext](#basecontext)
  - [NtlmPasswordAuthenticator](#ntlmpasswordauthenticator)
- [SmbResource interface](#smbresource-interface)
- [SmbFile](#smbfile)
- [SmbFileInputStream](#smbfileinputstream)
- [SmbFileOutputStream](#smbfileoutputstream)
- [SmbRandomAccessFile](#smbrandomaccessfile)
- [SmbException](#smbexception)
- [SmbConstants](#smbconstants)

---

## Setup

### PropertyConfiguration

`jcifs.config.PropertyConfiguration extends BaseConfiguration implements Configuration`

Builds the JCIFS configuration from a `java.util.Properties` map.

```java
PropertyConfiguration(Properties p) throws CIFSException
```

Pass an empty `new Properties()` to use all defaults (SMB2/3, standard timeouts, no pre-configured credentials).
Commonly useful properties:

| Property key | Description |
|---|---|
| `jcifs.smb.client.responseTimeout` | Response timeout in ms (default 30 000) |
| `jcifs.smb.client.soTimeout` | Socket timeout in ms (default 35 000) |
| `jcifs.smb.client.connTimeout` | Connection timeout in ms (default 35 000) |
| `jcifs.smb.client.minVersion` | Minimum SMB dialect (`SMB1`, `SMB202`, `SMB210`, `SMB300`, `SMB302`, `SMB311`) |
| `jcifs.smb.client.maxVersion` | Maximum SMB dialect |
| `jcifs.smb.client.useExtendedSecurity` | Enable SPNEGO/extended security (default `true`) |
| `jcifs.smb.client.signingEnforced` | Require SMB signing (default `false`) |
| `jcifs.netbios.wins` | WINS server address for NetBIOS name resolution |
| `jcifs.smb.client.domain` | Default domain for credentials |
| `jcifs.smb.client.username` | Default username |
| `jcifs.smb.client.password` | Default password |

---

### BaseContext

`jcifs.context.BaseContext extends AbstractCIFSContext`

The root CIFS context. Create once per application lifetime.

```java
BaseContext(Configuration config)
```

Initialises the transport pool, buffer cache, DFS resolver, and NetBIOS name service client.

#### Inherited from `AbstractCIFSContext`

```java
CIFSContext withCredentials(Credentials creds)
```
Returns a **child context** that wraps `creds` for all connections. Does not mutate the parent context.

```java
CIFSContext withAnonymousCredentials()
CIFSContext withGuestCrendentials()
CIFSContext withDefaultCredentials()
```
Convenience factory methods for common credential types.

```java
Credentials getCredentials()
boolean hasDefaultCredentials()
boolean renewCredentials(String locationHint, Throwable error)
boolean close() throws CIFSException
```

---

### NtlmPasswordAuthenticator

`jcifs.smb.NtlmPasswordAuthenticator implements Principal, CredentialsInternal`

Holds NTLM credentials for authenticating to SMB shares.

#### `AuthenticationType` enum

```java
enum AuthenticationType {
    NULL,   // anonymous (no credentials)
    GUEST,  // guest login (invalid credentials allowed, fallback to anonymous)
    USER    // regular username + password
}
```

#### Constructors

```java
NtlmPasswordAuthenticator()                                        // anonymous (NULL)
NtlmPasswordAuthenticator(AuthenticationType type)                 // explicit type
NtlmPasswordAuthenticator(String username, String password)        // USER, no domain
NtlmPasswordAuthenticator(String domain, String username, String password)  // USER
NtlmPasswordAuthenticator(String domain, String username, String password, AuthenticationType type)
```

The `username` string may embed the domain in `domain\user` or `user@domain` format —
the constructor will parse it automatically.
Pass `null` for `domain` to let the server determine the domain.

#### Getters

| Method | Description |
|---|---|
| `getUsername()` | Plain username (no domain prefix) |
| `getPassword()` | Plain text password |
| `getUserDomain()` | Domain name; empty string if not set |
| `getName()` | `domain\username` string |
| `isAnonymous()` | `true` if type is `NULL` |
| `isGuest()` | `true` if type is `GUEST` |

---

## SmbResource interface

`jcifs.SmbResource extends AutoCloseable`

The base interface for all SMB resources (files, directories, shares, servers, workgroups).
`SmbFile` is the main implementation.

### URL format

```
smb://[domain;username[:password]@]server[:port]/[share/[dir/]file]
```

**Trailing slash is mandatory for directories, shares, servers, and workgroups.**

### Lifecycle

```java
void close()
```
Releases the tree connection handle. In non-strict mode this is usually a no-op;
with `jcifs.smb.client.strictResourceLifecycle=true` it actively closes the connection.
Always use in `use {}` (Kotlin) or try-with-resources.

### Navigation

```java
String getName()               // last path component; includes trailing "/" for directories
int getType() throws CIFSException  // TYPE_FILESYSTEM, TYPE_WORKGROUP, TYPE_SERVER, TYPE_SHARE, TYPE_NAMED_PIPE, TYPE_PRINTER, TYPE_COMM
SmbResource resolve(String name) throws CIFSException  // child resource
```

### Existence and attributes

```java
boolean exists() throws CIFSException        // true if resource exists; results cached (default 5 s TTL)
boolean isFile() throws CIFSException
boolean isDirectory() throws CIFSException
boolean isHidden() throws CIFSException      // also true for admin shares ending in "$"
boolean canRead() throws CIFSException       // equivalent to exists()
boolean canWrite() throws CIFSException      // exists() && not read-only
int getAttributes() throws CIFSException     // bitmask of ATTR_* constants
long fileIndex() throws CIFSException        // server-side file index (inode number); 0 if unavailable
```

### Timestamps (milliseconds since epoch)

```java
long createTime() throws CIFSException       // 0 for root/share
long lastModified() throws CIFSException     // 0 for root/share
long lastAccess() throws CIFSException       // 0 for root/share
```

### Setting timestamps and attributes

```java
void setFileTimes(long createTime, long lastModified, long lastAccess) throws CIFSException
void setCreateTime(long time) throws CIFSException
void setLastModified(long time) throws CIFSException
void setLastAccess(long time) throws CIFSException
void setAttributes(int attrs) throws CIFSException
void setReadOnly() throws CIFSException      // shorthand: setAttributes(attrs | ATTR_READONLY)
void setReadWrite() throws CIFSException     // shorthand: setAttributes(attrs & ~ATTR_READONLY)
```

### Size and disk space

```java
long length() throws CIFSException           // file size in bytes; capacity in bytes for TYPE_SHARE; 0 for directories
long getDiskFreeSpace() throws CIFSException // free bytes on the share/drive; 0 for non-share types
```

### File and directory operations

```java
void createNewFile() throws CIFSException    // creates empty file; fails if already exists (atomic)
void mkdir() throws CIFSException            // creates a single directory; parent must exist; path must end with "/"
void mkdirs() throws CIFSException           // creates directory and all missing parents
void delete() throws CIFSException           // deletes file or recursively deletes directory; removes read-only flag first
void copyTo(SmbResource dest) throws CIFSException    // copies file/directory tree; cross-host supported
void renameTo(SmbResource dest) throws CIFSException           // rename/move; source and dest must be on same share
void renameTo(SmbResource dest, boolean replace) throws CIFSException  // replace=true requires SMB2
```

### Streams and random access

```java
InputStream openInputStream() throws CIFSException
InputStream openInputStream(int sharing) throws CIFSException
InputStream openInputStream(int flags, int access, int sharing) throws CIFSException

OutputStream openOutputStream() throws CIFSException              // truncate, write-only, sharable
OutputStream openOutputStream(boolean append) throws CIFSException
OutputStream openOutputStream(boolean append, int sharing) throws CIFSException
OutputStream openOutputStream(boolean append, int openFlags, int access, int sharing) throws CIFSException

SmbRandomAccess openRandomAccess(String mode) throws CIFSException          // mode: "r" or "rw"
SmbRandomAccess openRandomAccess(String mode, int sharing) throws CIFSException
```

### Directory enumeration

```java
CloseableIterator<SmbResource> children() throws CIFSException
CloseableIterator<SmbResource> children(String wildcard) throws CIFSException    // server-side pattern: "*", "*.txt", "doc?"
CloseableIterator<SmbResource> children(ResourceNameFilter filter) throws CIFSException
CloseableIterator<SmbResource> children(ResourceFilter filter) throws CIFSException
```

Always close the iterator when done (use in try-with-resources).

### Directory watching

```java
SmbWatchHandle watch(int filter, boolean recursive) throws CIFSException
```

Subscribes to directory change notifications. `filter` is a bitmask of `FileNotifyInformation` constants.
Only works on directories; requires SMB2 or `CAP_NT_SMBS`.

### Security

```java
ACE[] getSecurity() throws IOException                     // DACL ACEs, SIDs unresolved
ACE[] getSecurity(boolean resolveSids) throws IOException  // optionally resolve SID names
ACE[] getShareSecurity(boolean resolveSids) throws IOException  // share-level ACL (different from file ACL)
SID getOwnerUser() throws IOException
SID getOwnerUser(boolean resolve) throws IOException
SID getOwnerGroup() throws IOException
SID getOwnerGroup(boolean resolve) throws IOException
```

---

## SmbFile

`jcifs.smb.SmbFile extends URLConnection implements SmbResource, SmbConstants`

Main implementation of `SmbResource`. Instances are **immutable** — the path never changes
after construction.

### Constructors

```java
SmbFile(String url, CIFSContext tc) throws MalformedURLException
SmbFile(URL url, CIFSContext tc) throws MalformedURLException
SmbFile(SmbResource context, String name) throws MalformedURLException, UnknownHostException
```

The deprecated single-argument constructors (no `CIFSContext`) use the global singleton context
and should be avoided in new code.

### Additional methods beyond SmbResource

```java
String getParent()           // everything except the last path component; "smb://" for root
String getPath()             // full uncanonicalized URL string
String getCanonicalPath()    // URL with "." and ".." resolved
String getCanonicalUncPath() // Windows UNC style path with backslashes
String getUncPath()          // UNC path below the share (e.g. "\folder\file.txt")
String getShare()            // share name; null for smb://, workgroup, or server URLs
String getServer()           // server or workgroup name; null for smb://
String getServerWithDfs()    // server name after DFS resolution
String getDfsPath() throws SmbException  // DFS referral path; null if not under DFS
SmbTreeHandle getTreeHandle() throws CIFSException
void connect() throws IOException  // ensures tree is connected (from URLConnection)
```

### `listFiles()` and `list()` overloads

```java
String[] list() throws SmbException
String[] list(SmbFilenameFilter filter) throws SmbException

SmbFile[] listFiles() throws SmbException
SmbFile[] listFiles(String wildcard) throws SmbException
SmbFile[] listFiles(SmbFilenameFilter filter) throws SmbException
SmbFile[] listFiles(SmbFileFilter filter) throws SmbException
```

May return `null` if the path is not a directory or the listing fails. Always check for `null`.
`listFiles()` is more efficient than `list()` when you need file attributes.
With strict resource lifecycle, close each element after use.

---

## SmbFileInputStream

`jcifs.smb.SmbFileInputStream extends InputStream`

Sequential read-only stream for an SMB file. Unbuffered — wrap in `BufferedInputStream` if many
single-byte reads are expected.

### Constructors

```java
SmbFileInputStream(SmbFile file) throws SmbException
SmbFileInputStream(String url, CIFSContext tc) throws SmbException, MalformedURLException
```

### Methods

```java
int read() throws IOException                         // reads one byte; returns -1 at EOF
int read(byte[] b) throws IOException
int read(byte[] b, int off, int len) throws IOException
int readDirect(byte[] b, int off, int len) throws IOException  // same as read(b,off,len)
long skip(long n) throws IOException                  // seeks forward without network I/O; no EOF check
int available() throws IOException                    // always 0 for files; polls server for named pipes
void close() throws IOException
void open() throws CIFSException                      // explicitly ensures the file handle is open
```

---

## SmbFileOutputStream

`jcifs.smb.SmbFileOutputStream extends OutputStream`

Sequential write stream for an SMB file.

### Constructors

```java
SmbFileOutputStream(SmbFile file) throws SmbException             // truncate mode
SmbFileOutputStream(SmbFile file, boolean append) throws SmbException
```

### Methods

```java
void write(int b) throws IOException
void write(byte[] b) throws IOException
void write(byte[] b, int off, int len) throws IOException
void writeDirect(byte[] b, int off, int len, int flags) throws IOException  // bypass TransWaitNamedPipe; used for DCERPC
boolean isOpen()
void close() throws IOException
void open() throws CIFSException    // explicitly ensures the file handle is open
```

---

## SmbRandomAccessFile

`jcifs.smb.SmbRandomAccessFile implements SmbRandomAccess`

Random-access read/write for an SMB file. Supports both read (`"r"`) and read-write (`"rw"`) modes.
All offsets are 64-bit.

### Constructors

```java
SmbRandomAccessFile(SmbFile file, String mode) throws SmbException
SmbRandomAccessFile(String url, String mode, int sharing, CIFSContext tc) throws SmbException, MalformedURLException
```

`mode` is `"r"` (read-only) or `"rw"` (read-write). Any other value throws `IllegalArgumentException`.

### Methods

```java
// Positioning
long getFilePointer()          // current byte offset
void seek(long pos)            // move to byte offset (no network I/O)
int skipBytes(int n)           // move forward n bytes; returns n

// File size
long length() throws SmbException
void setLength(long newLength) throws SmbException   // truncates or extends the file

// Reading
int read() throws SmbException                       // single byte; -1 at EOF
int read(byte[] b) throws SmbException
int read(byte[] b, int off, int len) throws SmbException
void readFully(byte[] b) throws SmbException         // blocks until b.length bytes are read; throws SmbEndOfFileException at EOF
void readFully(byte[] b, int off, int len) throws SmbException

// Typed readers (big-endian)
boolean readBoolean() throws SmbException
byte readByte() throws SmbException
int readUnsignedByte() throws SmbException
short readShort() throws SmbException
int readUnsignedShort() throws SmbException
char readChar() throws SmbException
int readInt() throws SmbException
long readLong() throws SmbException
float readFloat() throws SmbException
double readDouble() throws SmbException
String readLine() throws SmbException                // reads up to newline; null at EOF
String readUTF() throws SmbException                 // reads length-prefixed modified UTF-8

// Writing
void write(int b) throws SmbException
void write(byte[] b) throws SmbException
void write(byte[] b, int off, int len) throws SmbException

// Typed writers (big-endian)
void writeBoolean(boolean v) throws SmbException
void writeByte(int v) throws SmbException
void writeShort(int v) throws SmbException
void writeChar(int v) throws SmbException
void writeInt(int v) throws SmbException
void writeLong(long v) throws SmbException
void writeFloat(float v) throws SmbException
void writeDouble(double v) throws SmbException
void writeBytes(String s) throws SmbException       // ASCII bytes only
void writeChars(String s) throws SmbException       // big-endian UTF-16
void writeUTF(String str) throws SmbException       // length-prefixed modified UTF-8

// Lifecycle
void close() throws SmbException
void open() throws CIFSException   // explicitly re-opens if closed
```

---

## SmbException

`jcifs.smb.SmbException extends CIFSException implements NtStatus, DosError, WinError`

Thrown by all jcifs-ng operations on failure. Carries an NT STATUS code.

### Constructors

```java
SmbException()
SmbException(String msg)
SmbException(String msg, Throwable rootCause)
SmbException(int errcode, Throwable rootCause)       // maps errcode to NT STATUS
SmbException(int errcode, boolean winerr)            // if winerr=true, treats errcode as Win32 error
```

### Key methods

```java
int getNtStatus()                               // NT STATUS code (e.g. 0xC0000034 = NOT_FOUND)
Throwable getRootCause()                        // deprecated; use getCause()
static String getMessageByCode(int errcode)     // look up NT STATUS message string
```

### Common NT STATUS codes

| Constant | Value | Meaning |
|---|---|---|
| `NT_STATUS_OK` | `0x00000000` | Success |
| `NT_STATUS_NO_SUCH_FILE` | `0xC000000F` | File not found |
| `NT_STATUS_OBJECT_NAME_NOT_FOUND` | `0xC0000034` | Object name not found |
| `NT_STATUS_OBJECT_NAME_COLLISION` | `0xC0000035` | File already exists |
| `NT_STATUS_OBJECT_PATH_NOT_FOUND` | `0xC000003A` | Path not found |
| `NT_STATUS_OBJECT_NAME_INVALID` | `0xC0000033` | Invalid filename |
| `NT_STATUS_ACCESS_DENIED` | `0xC0000022` | Access denied |
| `NT_STATUS_LOGON_FAILURE` | `0xC000006D` | Wrong username or password |
| `NT_STATUS_PIPE_BROKEN` | `0xC000014B` | Named pipe disconnected |
| `NT_STATUS_UNSUCCESSFUL` | `0xC0000001` | Generic failure |

---

## SmbConstants

`jcifs.SmbConstants` (interface — all fields are `static final int`)

Constants used when calling `openInputStream`, `openOutputStream`, `openRandomAccess`, `listFiles`, etc.

### Resource types (`getType()` return values)

| Constant | Value | Description |
|---|---|---|
| `TYPE_FILESYSTEM` | `0x01` | Regular file or directory |
| `TYPE_WORKGROUP` | `0x02` | NetBIOS workgroup |
| `TYPE_SERVER` | `0x04` | SMB server |
| `TYPE_SHARE` | `0x08` | Network share |
| `TYPE_NAMED_PIPE` | `0x10` | Named pipe |
| `TYPE_PRINTER` | `0x20` | Printer |
| `TYPE_COMM` | `0x40` | Communications device |

### File attributes (`ATTR_*`)

| Constant | Hex | Description |
|---|---|---|
| `ATTR_READONLY` | `0x01` | Read-only |
| `ATTR_HIDDEN` | `0x02` | Hidden |
| `ATTR_SYSTEM` | `0x04` | System file |
| `ATTR_VOLUME` | `0x08` | Volume label |
| `ATTR_DIRECTORY` | `0x10` | Directory |
| `ATTR_ARCHIVE` | `0x20` | Archive |
| `ATTR_NORMAL` | `0x080` | Normal (no other attributes set) |
| `ATTR_TEMPORARY` | `0x100` | Temporary |
| `ATTR_COMPRESSED` | `0x800` | Compressed |

### Share access flags (`FILE_SHARE_*`)

| Constant | Value | Description |
|---|---|---|
| `FILE_NO_SHARE` | `0x00` | Exclusive access |
| `FILE_SHARE_READ` | `0x01` | Allow concurrent reads |
| `FILE_SHARE_WRITE` | `0x02` | Allow concurrent writes |
| `FILE_SHARE_DELETE` | `0x04` | Allow concurrent deletes |
| `DEFAULT_SHARING` | `0x07` | Read + Write + Delete |

### Open flags (`O_*`)

| Constant | Hex | Description |
|---|---|---|
| `O_RDONLY` | `0x01` | Open for reading only |
| `O_WRONLY` | `0x02` | Open for writing only |
| `O_RDWR` | `0x03` | Open for reading and writing |
| `O_APPEND` | `0x04` | Append writes to end of file |
| `O_CREAT` | `0x0010` | Create if file does not exist |
| `O_EXCL` | `0x0020` | Fail if file already exists |
| `O_TRUNC` | `0x0040` | Truncate file on open |

### Access mask constants

| Constant | Hex | Description |
|---|---|---|
| `FILE_READ_DATA` | `0x00000001` | Read file data |
| `FILE_WRITE_DATA` | `0x00000002` | Write file data |
| `FILE_APPEND_DATA` | `0x00000004` | Append data |
| `FILE_READ_EA` | `0x00000008` | Read extended attributes |
| `FILE_WRITE_EA` | `0x00000010` | Write extended attributes |
| `FILE_EXECUTE` | `0x00000020` | Execute / traverse |
| `FILE_DELETE` | `0x00000040` | Delete file |
| `FILE_READ_ATTRIBUTES` | `0x00000080` | Read file attributes |
| `FILE_WRITE_ATTRIBUTES` | `0x00000100` | Write file attributes |
| `DELETE` | `0x00010000` | Delete object |
| `READ_CONTROL` | `0x00020000` | Read security descriptor |
| `WRITE_DAC` | `0x00040000` | Write DACL |
| `WRITE_OWNER` | `0x00080000` | Change owner |
| `SYNCHRONIZE` | `0x00100000` | Synchronize access |
| `GENERIC_READ` | `0x80000000` | Generic read |
| `GENERIC_WRITE` | `0x40000000` | Generic write |
| `GENERIC_EXECUTE` | `0x20000000` | Generic execute |
| `GENERIC_ALL` | `0x10000000` | Generic all |

### Networking defaults

| Constant | Value | Description |
|---|---|---|
| `DEFAULT_PORT` | `445` | SMB TCP port |
| `DEFAULT_RESPONSE_TIMEOUT` | `30000` | Response timeout in ms |
| `DEFAULT_SO_TIMEOUT` | `35000` | Socket timeout in ms |
| `DEFAULT_CONN_TIMEOUT` | `35000` | Connection timeout in ms |
