package it.wylke.binpastes.paste.api;

import it.wylke.binpastes.paste.api.model.CreateCmd;
import it.wylke.binpastes.paste.api.model.ListView;
import it.wylke.binpastes.paste.api.model.SingleView;
import it.wylke.binpastes.paste.domain.PasteService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@CrossOrigin
@Controller
@RequestMapping("/api/v1/paste")
class PasteController {

    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteService pasteService;

    private final Validator validator;

    @Autowired
    PasteController(final PasteService pasteService, final Validator validator) {
        this.pasteService = pasteService;
        this.validator = validator;
    }

    @GetMapping(value = "/{pasteId}")
    @ResponseBody
    public Mono<SingleView> findPaste(@PathVariable("pasteId") String pasteId) {
        return pasteService
                .find(pasteId)
                .map(SingleView::from)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{pasteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePaste(@PathVariable("pasteId") String pasteId) {
        pasteService.delete(pasteId);
    }

    @GetMapping("/search/{text}")
    @ResponseBody
    public Mono<ListView> findPastesByFullText(@PathVariable("text") String text) {
        if (!StringUtils.hasText(text) || text.strip().length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return pasteService
                .findByFullText(text)
                .map(SingleView::from)
                .collectList()
                .map(ListView::from);
    }

    @GetMapping
    @ResponseBody
    public Mono<ListView> findPastes() {
        return pasteService
                .findAll()
                .map(SingleView::from)
                .collectList()
                .map(ListView::from);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<RedirectView> createPaste(ServerWebExchange ctx) {
        return ctx
                .getFormData()
                .flatMap(formData -> {
                    var createCmd = new CreateCmd(
                        formData.getFirst("title"),
                        formData.getFirst("content"),
                        formData.getFirst("expiry")
                    );

                    // TODO evaluate validation ;)
                    Set<ConstraintViolation<CreateCmd>> validate = validator.validate(createCmd);
                    if (!validate.isEmpty()) {
                        log.warn(validate.toString());
                    }

                    return pasteService.create(
                            createCmd.content(),
                            createCmd.title(),
                            ctx.getRequest().getRemoteAddress().getAddress().getHostAddress(),
                            createCmd.dateOfExpiry()
                    );
                })
                .map(paste -> new RedirectView("/api/v1/paste/" + paste.getId()));
    }

}
