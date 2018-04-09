package cern.ais.gridwars.web.domain;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "match")
public class Match {

    public enum Status {
        PENDING, RUNNING, FINISHED, FAILED, CANCELLED
    }

    public enum Outcome {
        WIN, DRAW, LOSS, DNF
    }

    @Id
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(optional = false)
    private Bot player1;

    @ManyToOne(optional = false)
    private Bot player2;

    @Column
    @Enumerated(EnumType.STRING)
    private Outcome outcome;

    @Column(nullable = false)
    private Instant pendingSince;

    @Column
    private Instant started;

    @Column
    private Instant ended;

    @Column
    private Integer turns;

    public String getId() {
        return id;
    }

    public Match setId(String id) {
        this.id = id;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public Match setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Bot getPlayer1() {
        return player1;
    }

    public Match setPlayer1(Bot player1) {
        this.player1 = player1;
        return this;
    }

    public Bot getPlayer2() {
        return player2;
    }

    public Match setPlayer2(Bot player2) {
        this.player2 = player2;
        return this;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Match setOutcome(Outcome outcome) {
        this.outcome = outcome;
        return this;
    }

    public Instant getPendingSince() {
        return pendingSince;
    }

    public Match setPendingSince(Instant pendingSince) {
        this.pendingSince = pendingSince;
        return this;
    }

    public Instant getStarted() {
        return started;
    }

    public Match setStarted(Instant started) {
        this.started = started;
        return this;
    }

    public Instant getEnded() {
        return ended;
    }

    public Match setEnded(Instant ended) {
        this.ended = ended;
        return this;
    }

    public Integer getTurns() {
        return turns;
    }

    public Match setTurns(Integer turns) {
        this.turns = turns;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Match match = (Match) o;
        return Objects.equals(id, match.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
