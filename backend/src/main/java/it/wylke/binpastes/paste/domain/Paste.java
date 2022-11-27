package it.wylke.binpastes.paste.domain;

import jakarta.annotation.Generated;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@org.springframework.data.relational.core.mapping.Table("paste")
public class Paste implements Persistable<String> {

    private transient boolean isPersistend = true;

    @org.springframework.data.annotation.Id
    private String id;

    @CreatedDate
    private LocalDateTime dateCreated;

    private String remoteIp;

    private LocalDateTime expiry;

    private String title;
    private String content;

    public static Paste newInstance(String remoteIp, String content, String title) {
        var paste = new Paste();
        paste.isPersistend = false;
        paste.id = UUID.randomUUID().toString();
        paste.remoteIp = remoteIp;
        paste.expiry = LocalDateTime.now().plusDays(1); // TODO
        paste.title = title;
        paste.content = content;
        return paste;
    }

    public String getId() {
        return id.toString();
    }

    @Override
    @org.springframework.data.annotation.Transient
    public boolean isNew() {
        System.out.println(Thread.currentThread().toString() + " : " + !this.isPersistend);
        return !this.isPersistend;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public String getRemoteIp() {
        return remoteIp;
    }
}
