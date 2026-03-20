# WebDAV — sardine-android

**Package:** `com.thegrizzlylabs.sardineandroid`
**Library:** `com.thegrizzlylabs:sardine-android`

API extracted from the compiled classes in `sardine.aar`.

---

## Table of Contents

- [OkHttpSardine](#okhttpsardine)
- [Sardine interface](#sardine-interface)
  - [Credentials](#credentials)
  - [Listing resources](#listing-resources)
  - [Downloading](#downloading)
  - [Uploading](#uploading)
  - [Moving / copying](#moving--copying)
  - [Deleting / creating directories](#deleting--creating-directories)
  - [Existence check](#existence-check)
  - [Locking](#locking)
  - [Custom properties](#custom-properties)
  - [Access control (ACL)](#access-control-acl)
  - [Quota](#quota)
  - [Sync / search / report](#sync--search--report)
  - [Compression / cookies](#compression--cookies)
- [DavResource](#davresource)
- [DavAcl](#davacl)
- [DavAce](#davace)
- [DavPrincipal](#davprincipal)
- [DavQuota](#davquota)
- [SardineException](#sardineexception)
- [SardineReport](#sardinereport)
- [SyncCollectionReport](#synccollectionreport)

---

## OkHttpSardine

`com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine implements Sardine`

The concrete WebDAV client backed by OkHttp. Implements all methods in the `Sardine` interface.

```java
OkHttpSardine()
OkHttpSardine(OkHttpClient client)  // bring your own OkHttpClient (custom timeouts, interceptors, etc.)
```

---

## Sardine interface

`com.thegrizzlylabs.sardineandroid.Sardine`

### Credentials

```java
void setCredentials(String username, String password)
void setCredentials(String username, String password, boolean preemptive)
```

Sets HTTP Basic/Digest credentials for all subsequent requests.
When `preemptive` is `true`, the `Authorization` header is sent on the first request
without waiting for a 401 challenge. Default is non-preemptive (challenge-response).

---

### Listing resources

```java
List<DavResource> list(String url) throws IOException
List<DavResource> list(String url, int depth) throws IOException
List<DavResource> list(String url, int depth, boolean includeRoot) throws IOException
List<DavResource> list(String url, int depth, Set<QName> props) throws IOException
```

Sends a `PROPFIND` request. Default `depth` is 1 (direct children only).
**The first element is always the directory itself** — use `.drop(1)` to skip it.
When `includeRoot` is `false` and depth ≥ 1, the root entry is omitted.
The `props` overload requests only specific DAV properties rather than `allprop`.
Throws `IOException` on network or server errors.

```java
List<DavResource> getResources(String url) throws IOException
```

Alias for `list(url)`.

```java
List<DavResource> propfind(String url, int depth, Set<QName> props) throws IOException
```

Like `list` but always requires an explicit property set. Lower-level than `list`.

---

### Downloading

```java
InputStream get(String url) throws IOException
InputStream get(String url, Map<String, String> headers) throws IOException
```

Downloads the resource at `url` and returns an `InputStream`.
The caller **must close** the stream when done to release the connection.
Throws `IOException` on failure.

---

### Uploading

```java
void put(String url, byte[] data) throws IOException
void put(String url, byte[] data, String contentType) throws IOException
void put(String url, File file, String contentType) throws IOException
void put(String url, File file, String contentType, boolean replace) throws IOException
void put(String url, File file, String contentType, boolean replace, String lockToken) throws IOException
```

Uploads content to `url` via HTTP `PUT`.
When `replace` is `false` and the resource already exists the server may reject with `412 Precondition Failed`.
`lockToken` is required when the target file is locked.
Throws `IOException` on failure.

---

### Moving / copying

```java
void move(String sourceUrl, String destinationUrl) throws IOException
void move(String sourceUrl, String destinationUrl, boolean overwrite) throws IOException
void move(String sourceUrl, String destinationUrl, boolean overwrite, String lockToken) throws IOException
```

Sends a `MOVE` request (server-side rename/move). Default `overwrite` is `true`.
Throws `IOException` on failure (source not found, destination conflict when `overwrite` is `false`, etc.).

```java
void copy(String sourceUrl, String destinationUrl) throws IOException
void copy(String sourceUrl, String destinationUrl, boolean overwrite) throws IOException
```

Sends a `COPY` request. The original resource is kept at `sourceUrl`.

---

### Deleting / creating directories

```java
void delete(String url) throws IOException
```

Sends HTTP `DELETE`. Servers delete directories recursively. Throws `IOException` on failure.

```java
void createDirectory(String url) throws IOException
```

Sends HTTP `MKCOL` to create a collection. The parent must already exist.
Throws `IOException` on failure.

---

### Existence check

```java
boolean exists(String url) throws IOException
```

Returns `true` if the resource exists, `false` on 404.
Throws `IOException` on network errors (not on 404).

---

### Locking

```java
String lock(String url) throws IOException
String lock(String url, int timeout) throws IOException
```

Acquires a WebDAV lock on `url`. Returns the lock token string.
`timeout` is in seconds; use `-1` for `Infinite`.

```java
String refreshLock(String url, String token, String timeout) throws IOException
```

Refreshes an existing lock. `token` is the value returned by `lock()`.

```java
void unlock(String url, String token) throws IOException
```

Releases a lock.

---

### Custom properties

```java
void setCustomProps(String url, Map<String, String> props, List<String> remove) throws IOException
```

Sets (or removes) custom DAV properties on a resource via `PROPPATCH`.

```java
List<DavResource> patch(String url, Map<QName, String> updates) throws IOException
List<DavResource> patch(String url, Map<QName, String> updates, List<QName> remove) throws IOException
List<DavResource> patch(String url, List<Element> set, List<QName> remove) throws IOException
```

Updates namespace-qualified properties via `PROPPATCH`. Returns the updated resource list.

---

### Access control (ACL)

```java
DavAcl getAcl(String url) throws IOException
```

Retrieves the ACL (owner, group, ACEs) for the resource.

```java
void setAcl(String url, List<DavAce> aces) throws IOException
```

Replaces the ACL on the resource.

```java
List<DavPrincipal> getPrincipals(String url) throws IOException
```

Lists principals (users/groups) available on the server.

```java
List<String> getPrincipalCollectionSet(String url) throws IOException
```

Returns the set of URLs that are principal collections.

---

### Quota

```java
DavQuota getQuota(String url) throws IOException
```

Returns the quota information (used / available bytes) for the resource.

---

### Sync / search / report

```java
<T> T report(String url, int depth, SardineReport<T> report) throws IOException
```

Sends a DAV `REPORT` request. Use with `SyncCollectionReport` for delta sync.

```java
List<DavResource> search(String url, String language, String query) throws IOException
```

Sends a `SEARCH` request. `language` is typically `"DAV:basicsearch"`.

---

### Compression / cookies

```java
void enableCompression()   // enable Accept-Encoding: gzip
void disableCompression()  // disable (default)
void ignoreCookies()       // do not store/send cookies
```

---

## DavResource

`com.thegrizzlylabs.sardineandroid.DavResource`

Represents a single WebDAV resource returned by `list()` or `propfind()`.

### Constants

| Constant | Value | Description |
|---|---|---|
| `DEFAULT_CONTENT_TYPE` | `"application/octet-stream"` | Fallback MIME type |
| `DEFAULT_CONTENT_LENGTH` | `0L` | Fallback size |
| `HTTPD_UNIX_DIRECTORY_CONTENT_TYPE` | `"httpd/unix-directory"` | Alternative directory indicator |
| `DEFAULT_STATUS_CODE` | `200` | HTTP status when not explicitly provided |

### Getters

| Method | Return type | Description |
|---|---|---|
| `getHref()` | `URI` | Full resource URI |
| `getName()` | `String` | Filename only (last path component, URL-decoded) |
| `getPath()` | `String` | Full path component of the URL |
| `getStatusCode()` | `int` | HTTP status code from multi-status response |
| `getCreation()` | `Date` | Creation date; `null` if not provided |
| `getModified()` | `Date` | Last-modified date; `null` if not provided |
| `getContentType()` | `String` | MIME type; falls back to `DEFAULT_CONTENT_TYPE` |
| `getContentLength()` | `Long` | File size in bytes; `null` for collections |
| `getEtag()` | `String` | ETag value; `null` if not provided |
| `getContentLanguage()` | `String` | Content-Language header value |
| `getDisplayName()` | `String` | Human-readable display name |
| `getResourceTypes()` | `List<QName>` | DAV resource type elements (e.g. `{DAV:}collection`) |
| `isDirectory()` | `boolean` | `true` if the resource is a collection |
| `getCustomProps()` | `Map<String, String>` | Custom properties as local-name → value |
| `getCustomPropsNS()` | `Map<QName, String>` | Custom properties with full namespace |
| `getLockDiscovery()` | `Lockdiscovery` | Active lock information |
| `getSupportedlock()` | `Supportedlock` | Supported lock types |

---

## DavAcl

`com.thegrizzlylabs.sardineandroid.DavAcl`

Represents the Access Control List of a DAV resource. Returned by `getAcl()`.

```java
DavAcl(Response response)
```

| Method | Return type | Description |
|---|---|---|
| `getOwner()` | `String` | DAV owner principal URL |
| `getGroup()` | `String` | DAV group principal URL |
| `getAces()` | `List<DavAce>` | Access control entries |

---

## DavAce

`com.thegrizzlylabs.sardineandroid.DavAce`

A single access control entry within a `DavAcl`.

```java
DavAce(DavPrincipal principal)
DavAce(Ace model)
```

| Method | Return type | Description |
|---|---|---|
| `getPrincipal()` | `DavPrincipal` | The principal this ACE applies to |
| `getGranted()` | `List<String>` | List of granted privilege names |
| `getDenied()` | `List<String>` | List of denied privilege names |
| `getInherited()` | `String` | URL of the resource this ACE is inherited from; `null` if not inherited |
| `isProtected()` | `boolean` | `true` if this ACE cannot be removed |
| `toModel()` | `Ace` | Converts back to the JAXB model object |

---

## DavPrincipal

`com.thegrizzlylabs.sardineandroid.DavPrincipal`

Represents a DAV principal (user, group, or special identifier).

### Constants

| Constant | Description |
|---|---|
| `KEY_SELF` | The principal that represents the authenticated user |
| `KEY_UNAUTHENTICATED` | Unauthenticated users |
| `KEY_AUTHENTICATED` | All authenticated users |
| `KEY_ALL` | All users (authenticated and unauthenticated) |

### `PrincipalType` enum

```java
enum PrincipalType { HREF, PROPERTY, SELF, UNAUTHENTICATED, AUTHENTICATED, ALL }
```

### Constructors

```java
DavPrincipal(PrincipalType type, String value, String displayName)
DavPrincipal(PrincipalType type, QName property, String displayName)
DavPrincipal(Principal model)
```

| Method | Return type | Description |
|---|---|---|
| `getPrincipalType()` | `PrincipalType` | The type of principal |
| `getValue()` | `String` | The principal URL (for `HREF` type) |
| `getProperty()` | `QName` | The property name (for `PROPERTY` type) |
| `getDisplayName()` | `String` | Human-readable name |

---

## DavQuota

`com.thegrizzlylabs.sardineandroid.DavQuota`

Disk quota information. Returned by `getQuota()`.

```java
DavQuota(Response response)
```

| Method | Return type | Description |
|---|---|---|
| `getQuotaAvailableBytes()` | `long` | Free bytes available to this principal |
| `getQuotaUsedBytes()` | `long` | Bytes already used |

---

## SardineException

`com.thegrizzlylabs.sardineandroid.impl.SardineException extends IOException`

Thrown by `OkHttpSardine` when the server returns a non-2xx HTTP status.

```java
SardineException(String message, int statusCode, String responsePhrase)
```

| Method | Return type | Description |
|---|---|---|
| `getStatusCode()` | `int` | HTTP status code (e.g. 404, 409, 423) |
| `getResponsePhrase()` | `String` | HTTP reason phrase |
| `getMessage()` | `String` | Combined message including status code |

---

## SardineReport

`com.thegrizzlylabs.sardineandroid.report.SardineReport<T>` (abstract)

Base class for DAV `REPORT` request bodies. Subclass to implement custom reports.

```java
String toXml() throws IOException        // serialise report body to XML string
abstract Object toJaxb()                 // return JAXB object for XML marshalling
abstract T fromMultistatus(Multistatus)  // parse the multi-status response
```

---

## SyncCollectionReport

`com.thegrizzlylabs.sardineandroid.report.SyncCollectionReport extends SardineReport<SyncCollectionReport.Result>`

Built-in report for WebDAV Collection Synchronisation (RFC 6578). Sends a `sync-collection`
report to get only resources changed since a given sync token.

### `SyncLevel` enum

```java
enum SyncLevel { ONE("1"), INFINITE("infinite") }
```

### Constructor

```java
SyncCollectionReport(
    String syncToken,          // empty string "" for initial full sync
    SyncLevel syncLevel,       // ONE or INFINITE
    Set<QName> properties,     // DAV properties to retrieve for each changed resource
    Integer limit              // optional max results; null for no limit
)
```

### `Result` inner class

```java
class Result {
    String getNewSyncToken()          // token to use in the next sync request
    List<DavResource> getChanged()    // added or modified resources
    List<DavResource> getDeleted()    // removed resources (status 404)
}
```

Use with `sardine.report(url, 0, new SyncCollectionReport(...))`.
