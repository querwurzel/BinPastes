package it.wylke.binpastes.paste.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table(Paste.TABLE_NAME)
public class Paste implements Persistable<String> {

    static final String TABLE_NAME = "pastes";

    @Id
    protected String id;
    @CreatedDate
    protected LocalDateTime dateCreated;

    protected String content;
    protected String title;
    protected LocalDateTime expiry;

    protected String remoteIp;
    protected Boolean isDeleted;

    public static Paste newInstance(String content, String title, String remoteIp, LocalDateTime expiry) {
        var paste = new NewPaste();
        paste.id = UUID.randomUUID().toString();
        paste.isDeleted = Boolean.FALSE;
        paste.content = content;
        paste.title = title;
        paste.remoteIp = remoteIp;
        paste.expiry = expiry;
        return paste;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    @JsonIgnore
    @Transient
    public boolean isNew() {
        return false;
    }

    public LocalDateTime getDateCreated() {
        return this.dateCreated;
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

    public Paste markAsDeleted() {
        this.isDeleted = true;
        return this;
    }



    // TODO remove
    public String getRemoteIp() {
        return remoteIp;
    }
    // TODO remove
    public boolean isDeleted() {
        return this.isDeleted;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Paste paste = (Paste) o;
        return Objects.equals(id, paste.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }



    static Paste fullTextExample(String text) {
        return FullTextTemplate.from(text);
    }

    private static final class FullTextTemplate extends Paste {

        public static Paste from(String text) {
            var paste = new FullTextTemplate();
            paste.content = text;
            paste.title = text;
            paste.isDeleted = false;
            return paste;
        }

    }

    private static final class NewPaste extends Paste implements Persistable<String> {

        @Override
        public String getId() {
            return super.getId();
        }

        @Override
        public boolean isNew() {
            return true;
        }

    }
}
