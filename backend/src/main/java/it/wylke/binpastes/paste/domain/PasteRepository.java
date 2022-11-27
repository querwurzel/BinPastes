package it.wylke.binpastes.paste.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PasteRepository extends R2dbcRepository<Paste, String> {}

//@Repository
//public interface PasteRepository extends CrudRepository<Paste, String> {}
