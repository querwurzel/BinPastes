package com.github.binpastes.paste.application;

import com.github.binpastes.paste.application.model.CreateCmd;
import com.github.binpastes.paste.application.model.DetailView;
import com.github.binpastes.paste.application.model.ListView;
import com.github.binpastes.paste.application.model.ListView.ListItemView;
import com.github.binpastes.paste.application.model.SearchView;
import com.github.binpastes.paste.application.model.SearchView.SearchItemView;
import com.github.binpastes.paste.application.tracking.TrackingService;
import com.github.binpastes.paste.domain.Paste;
import com.github.binpastes.paste.domain.PasteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PasteViewService {

    private static final Logger log = LoggerFactory.getLogger(PasteViewService.class);

    private final PasteService pasteService;
    private final TrackingService trackingService;

    @Autowired
    public PasteViewService(PasteService pasteService, TrackingService trackingService) {
        this.pasteService = pasteService;
        this.trackingService = trackingService;
    }

    public Mono<DetailView> viewPaste(String id, String remoteAddress) {
        return pasteService.find(id)
                .doOnNext(paste -> {
                    if (paste.isPublic()) {
                        trackingService.trackView(paste.getId());
                    }
                })
                .map(paste -> {
                    if (paste.isOneTime()) {
                        return toOneTimeView(paste, remoteAddress);
                    } else {
                        return DetailView.of(paste, remoteAddress);
                    }
                });
    }

    public Mono<DetailView> viewOneTimePaste(String id) {
        return pasteService.findAndBurn(id)
                .map(paste -> new DetailView(
                        paste.getId(),
                        paste.getTitle(),
                        paste.getContent(),
                        paste.getContent().getBytes().length,
                        paste.isPublic(),
                        false, // paste just burnt
                        paste.isEncrypted(),
                        paste.isOneTime(),
                        paste.isPermanent(),
                        paste.getDateCreated(),
                        paste.getDateOfExpiry(),
                        paste.getLastViewed(),
                        paste.getViews()
                ));
    }

    public Mono<ListView> viewAllPastes() {
        return pasteService.findAll()
                .map(ListItemView::of)
                .collectList()
                .map(ListView::of);
    }

    public Mono<SearchView> searchByFullText(String term) {
        return pasteService.findByFullText(term)
                .map(paste -> SearchItemView.of(paste, term))
                .collectList()
                .map(SearchView::of)
                .doOnSuccess(searchView -> log.info("Found {} pastes searching for: {}", searchView.pastes().size(), term));
    }

    public Mono<DetailView> createPaste(CreateCmd cmd, String remoteAddress) {
        return pasteService.create(
                        cmd.title(),
                        cmd.content(),
                        cmd.dateOfExpiry(),
                        cmd.isEncrypted(),
                        cmd.pasteExposure(),
                        remoteAddress
                )
                .map(paste -> {
                    if (paste.isOneTime()) {
                        return toOneTimeView(paste, remoteAddress);
                    } else {
                        return DetailView.of(paste, remoteAddress);
                    }
                });
    }

    public Mono<Void> requestDeletion(String id, String remoteAddress) {
        return pasteService.requestDeletion(id, remoteAddress);
    }

    private static DetailView toOneTimeView(Paste reference, String remoteAddress) {
        return new DetailView(
                reference.getId(),
                null,
                null,
                0,
                reference.isPublic(),
                reference.isErasable(remoteAddress),
                reference.isEncrypted(),
                reference.isOneTime(),
                reference.isPermanent(),
                reference.getDateCreated(),
                reference.getDateOfExpiry(),
                reference.getLastViewed(),
                reference.getViews()
        );
    }
}
