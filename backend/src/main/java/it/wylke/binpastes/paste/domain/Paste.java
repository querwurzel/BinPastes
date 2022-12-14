package it.wylke.binpastes.paste.domain;

import it.wylke.binpastes.util.IdGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

import static it.wylke.binpastes.paste.domain.Paste.PasteSchema;

@Table(PasteSchema.TABLE_NAME)
public class Paste {

    @Id
    @Column(PasteSchema.ID)
    private String id;
    @Version
    @Column(PasteSchema.VIEWS)
    private Long views;
    @Column(PasteSchema.TITLE)
    private String title;
    @Column(PasteSchema.CONTENT)
    private String content;
    @Column(PasteSchema.IS_ENCRYPTED)
    private boolean isEncrypted;
    @Column(PasteSchema.EXPOSURE)
    private PasteExposure exposure;

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

    public static Paste newInstance(
            String title,
            String content,
            LocalDateTime dateOfExpiry,
            boolean isEncrypted,
            PasteExposure exposure,
            String remoteAddress
    ) {
        return new Paste()
                .setId(IdGenerator.newStringId())
                .setTitle(title)
                .setContent(Objects.requireNonNull(content))
                .setIsEncrypted(isEncrypted)
                .setExposure(Objects.requireNonNull(exposure))
                .setRemoteAddress(remoteAddress)
                .setDateOfExpiry(dateOfExpiry);
    }

    public String getId() {
        return id;
    }

    public Long getViews() {
        return views;
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

    public LocalDateTime getDateOfExpiry() {
        return dateOfExpiry;
    }

    public boolean isEncrypted() {
        return this.isEncrypted;
    }

    public PasteExposure getExposure() {
        return exposure;
    }

    public boolean isOneTime() {
        return exposure == PasteExposure.ONCE;
    }

    public Paste markAsExpired() {
        this.dateOfExpiry = LocalDateTime.now();
        return this;
    }

    protected Paste setId(final String id) {
        this.id = id;
        return this;
    }

    protected Paste setViews(final Long views) {
        this.views = views;
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

    protected Paste setExposure(final PasteExposure exposure) {
        this.exposure = exposure;
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

    public enum PasteExposure {
        PUBLIC,
        UNLISTED,
        ONCE
    }

    public static final class PasteSchema {

        public static final String TABLE_NAME = "pastes";

        public static final String ID = "id";
        public static final String VIEWS = "views";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String IS_ENCRYPTED = "is_encrypted";
        public static final String EXPOSURE = "exposure";
        public static final String DATE_CREATED = "date_created";
        public static final String DATE_OF_EXPIRY = "date_of_expiry";
        public static final String DATE_DELETED = "date_deleted";
        public static final String REMOTE_ADDRESS = "remote_address";

    }
}
