package cern.ais.gridwars.web.domain;

import cern.ais.gridwars.web.util.DomainUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;


@Entity
public class Match {

    public enum Status {
        /**
         * Pending to be executed
         */
        PENDING,

        /**
         * Currently running
         */
        RUNNING,

        /**
         * Finished without errors
         */
        FINISHED,

        /**
         * Failed with an error or abnormal termination
         */
        FAILED,

        /**
         * Cancelled before is could be executed
         */
        CANCELLED
    }

    public enum Outcome {
        /**
         * Bot 1 won
         */
        WIN,

        /**
         * Bot 1 lost (implies that bot 2 won)
         */
        LOSS,

        /**
         * Draw
         */
        DRAW,

        /**
         * Did not finish (error or abnormal termination)
         */
        DNF
    }

    @Id
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(optional = false)
    private Bot bot1;

    @ManyToOne(optional = false)
    private Bot bot2;

    @Enumerated(EnumType.STRING)
    private Outcome outcome;

    @Column(nullable = false)
    private Instant pendingSince;

    private Instant started;

    private Instant ended;

    private Integer turns;

    @Size(max = 1024)
    private String failReason;

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

    public Bot getBot1() {
        return bot1;
    }

    public Match setBot1(Bot bot1) {
        this.bot1 = bot1;
        return this;
    }

    public Bot getBot2() {
        return bot2;
    }

    public Match setBot2(Bot bot2) {
        this.bot2 = bot2;
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

    public String getFailReason() {
        return failReason;
    }

    public Match setFailReason(String failReason) {
        this.failReason = DomainUtils.truncate(failReason, 1024);
        return this;
    }

    public Long getDurationMillis() {
        return (started != null && ended != null) ? Duration.between(started, ended).toMillis() : null;
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
