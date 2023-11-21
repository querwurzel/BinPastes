package com.github.binpastes.paste.domain;

import com.github.binpastes.util.IdGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.github.binpastes.paste.domain.Paste.PasteSchema;
import static java.util.Objects.isNull;

@Table(PasteSchema.TABLE_NAME)
public class Paste {

    @Id
    @Column(PasteSchema.ID)
    private String id;
    @Version
    @Column(PasteSchema.VERSION)
    private Long version;
    @Column(PasteSchema.TITLE)
    private String title;
    @Column(PasteSchema.CONTENT)
    private String content;
    @Column(PasteSchema.IS_ENCRYPTED)
    private Boolean isEncrypted;
    @Column(PasteSchema.EXPOSURE)
    private PasteExposure exposure;

    @CreatedDate
    @Column(PasteSchema.DATE_CREATED)
    private LocalDateTime dateCreated;
    @Column(PasteSchema.DATE_OF_EXPIRY)
    private LocalDateTime dateOfExpiry;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    @Column(PasteSchema.DATE_DELETED)
    private LocalDateTime dateDeleted;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    @Column(PasteSchema.REMOTE_ADDRESS)
    private String remoteAddress;

    @Column(PasteSchema.LAST_VIEWED)
    private LocalDateTime lastViewed;
    @Column(PasteSchema.VIEWS)
    private Long views;

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
                .setDateOfExpiry(dateOfExpiry)
                .setViews(0);
    }

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

    public LocalDateTime getDateOfExpiry() {
        return dateOfExpiry;
    }

    protected PasteExposure getExposure() {
        return exposure;
    }

    protected String getRemoteAddress() {
        return remoteAddress;
    }

    public LocalDateTime getLastViewed() {
        return lastViewed;
    }

    public long getViews() {
        return views;
    }

    public boolean isEncrypted() {
        return this.isEncrypted;
    }

    public boolean isPublic() {
        return this.exposure == PasteExposure.PUBLIC;
    }

    public boolean isUnlisted() {
        return this.exposure == PasteExposure.UNLISTED;
    }

    public boolean isOneTime() {
        return this.exposure == PasteExposure.ONCE;
    }

    public boolean isPermanent() {
        return isNull(this.dateOfExpiry);
    }

    public boolean isErasable(String remoteAddress) {
        if (isUnlisted() || isOneTime()) {
            return true;
        }

        if (isPublic()) {
            final var createdBySameAuthor = Objects.equals(remoteAddress, getRemoteAddress());

            if (createdBySameAuthor) {
                return LocalDateTime.now().minusHours(1).isBefore(getDateCreated());
            }
        }

        return false;
    }

    public Paste trackView(LocalDateTime lastViewed) {
        if (isNull(getLastViewed()) || getLastViewed().isBefore(lastViewed)) {
            setLastViewed(lastViewed);
        }

        return setViews(this.getViews() + 1);
    }

    public Paste markAsExpired() {
        if (isNull(getDateOfExpiry())) {
            return setDateOfExpiry(LocalDateTime.now());
        }

        throw new IllegalStateException("Paste is already expired");
    }

    protected Paste setId(final String id) {
        this.id = id;
        return this;
    }

    protected void setVersion(final long version) {
        this.version = version;
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

    protected Paste setDateOfExpiry(final LocalDateTime dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
        return this;
    }

    protected void setDateCreated(final LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    protected void setDateDeleted(final LocalDateTime dateDeleted) {
        this.dateDeleted = dateDeleted;
    }

    protected void setLastViewed(final LocalDateTime lastViewed) {
        this.lastViewed = lastViewed;
    }

    protected Paste setViews(final long views) {
        this.views = views;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (isNull(o) || getClass() != o.getClass()) return false;
        Paste paste = (Paste) o;
        return Objects.equals(id, paste.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Paste{");
        sb.append("id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public enum PasteExposure {
        PUBLIC,
        UNLISTED,
        ONCE
    }

    public static final class PasteSchema {

        public static final String TABLE_NAME = "pastes";

        public static final String ID = "id";
        public static final String VERSION = "version";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String IS_ENCRYPTED = "is_encrypted";
        public static final String EXPOSURE = "exposure";
        public static final String DATE_CREATED = "date_created";
        public static final String DATE_OF_EXPIRY = "date_of_expiry";
        public static final String DATE_DELETED = "date_deleted";
        public static final String REMOTE_ADDRESS = "remote_address";
        public static final String LAST_VIEWED = "last_viewed";
        public static final String VIEWS = "views";

    }
}
