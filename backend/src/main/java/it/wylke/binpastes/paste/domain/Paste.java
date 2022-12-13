package it.wylke.binpastes.paste.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.wylke.binpastes.util.IdGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

import static it.wylke.binpastes.paste.domain.Paste.PasteSchema;

@Table(PasteSchema.TABLE_NAME)
public class Paste implements Persistable<String> {

    @Id
    @Column(PasteSchema.ID)
    private String id;
    @Column(PasteSchema.TITLE)
    private String title;
    @Column(PasteSchema.CONTENT)
    private String content;
    @Column(PasteSchema.IS_ENCRYPTED)
    private boolean isEncrypted;

    @CreatedDate
    @Column(PasteSchema.DATE_CREATED)
    private LocalDateTime dateCreated;
    @Column(PasteSchema.DATE_OF_EXPIRY)
    private LocalDateTime dateOfExpiry;
    @SuppressWarnings("unused")
    @Column(PasteSchema.DATE_DELETED)
    private LocalDateTime dateDeleted;
    @SuppressWarnings("unused")
    @Column(PasteSchema.REMOTE_ADDRESS)
    private String remoteAddress;

    public static Paste newInstance(String content, String title, String remoteIp, LocalDateTime dateOfExpiry) {
        return NewPaste.newInstance(content, title, remoteIp, dateOfExpiry);
    }

    @Override
    @JsonIgnore
    @Transient
    public boolean isNew() {
        return false;
    }

    @Override
    public String getId() {
        return id;
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

    public boolean isEncrypted() {
        return this.isEncrypted;
    }

    public LocalDateTime getDateOfExpiry() {
        return dateOfExpiry;
    }

    public Paste markAsDeleted() {
        this.dateDeleted = LocalDateTime.now();
        return this;
    }

    protected Paste setId(final String id) {
        this.id = id;
        return this;
    }

    protected Paste setTitle(final String title) {
        this.title = title;
        return this;
    }

    protected Paste setContent(final String content) {
        this.content = content;
        return this;
    }

    protected Paste setIsEncrypted(final boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
        return this;
    }

    protected Paste setRemoteAddress(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    protected Paste setDateCreated(final LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    protected Paste setDateOfExpiry(final LocalDateTime dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
        return this;
    }

    protected Paste setDateDeleted(final LocalDateTime dateDeleted) {
        this.dateDeleted = dateDeleted;
        return this;
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

    private static final class NewPaste extends Paste implements Persistable<String> {

        public static Paste newInstance(String content, String title, String remoteIp, LocalDateTime dateOfExpiry) {
            return new NewPaste()
                    .setId(IdGenerator.newStringId())
                    .setTitle(title)
                    .setContent(Objects.requireNonNull(content))
                    .setIsEncrypted(false)
                    .setRemoteAddress(remoteIp)
                    .setDateOfExpiry(dateOfExpiry);
        }

        @Override
        public boolean isNew() {
            return true;
        }
    }

    static final class PasteSchema {

        public static final String TABLE_NAME = "pastes";

        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String IS_ENCRYPTED = "is_encrypted";
        public static final String DATE_CREATED = "date_created";
        public static final String DATE_OF_EXPIRY = "date_of_expiry";
        public static final String DATE_DELETED = "date_deleted";
        public static final String REMOTE_ADDRESS = "remote_address";

    }
}
