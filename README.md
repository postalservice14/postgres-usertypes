# Postgres User Types for [Hibernate](http://hibernate.org/)

[![](https://jitpack.io/v/postalservice14/postgres-usertypes.svg)](https://jitpack.io/#postalservice14/postgres-usertypes)

## Download

### Maven

Add the repository
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency

```xml
<dependency>
    <groupId>com.github.postalservice14</groupId>
    <artifactId>postgres-usertypes</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add to your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency

```
dependencies {
    implementation 'com.github.postalservice14:postgres-usertypes:1.0.0'
}
```

## Usage

### JSON List

```java
@Type(type = "com.postalservice14.jpa.usertype.JsonbObjectListUserType", parameters = {@org.hibernate.annotations.Parameter(name = "type", value = "LIST"), @org.hibernate.annotations.Parameter(name = "element", value = "java.lang.Integer")})
private List<Integer> numbers;
```

### XML Object

```java
@Type(type = "com.postalservice14.jpa.usertype.XmlUserTypeSupport")
private CustomObject xmlDataColumn;
```