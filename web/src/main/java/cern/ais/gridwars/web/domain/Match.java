package cern.ais.gridwars.web.domain;

import cern.ais.gridwars.web.util.DomainUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private Instant cancelled;

    private Integer turnCount;

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

    public LocalDateTime getStartedDateTime() {
        return LocalDateTime.ofInstant(started, ZoneId.systemDefault());
    }

    public Instant getEnded() {
        return ended;
    }

    public Match setEnded(Instant ended) {
        this.ended = ended;
        return this;
    }

    public Instant getCancelled() {
        return cancelled;
    }

    public Match setCancelled(Instant cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public Integer getTurnCount() {
        return turnCount;
    }

    public Match setTurnCount(Integer turnCount) {
        this.turnCount = turnCount;
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

    public boolean isFailed() {
        return (Status.FAILED == status);
    }

    public boolean isFinished() {
        return (Status.FINISHED == status);
    }

    public boolean isBot1Winner() {
        return isFinished() && (Outcome.WIN == outcome);
    }

    public boolean isBot2Winner() {
        return isFinished() && (Outcome.LOSS == outcome);
    }

    public boolean isDraw() {
        return isFinished() && (Outcome.DRAW == outcome);
    }

    public boolean isBotWinner(Bot bot) {
        return (isBot1Winner() && bot1.equals(bot)) || (isBot2Winner() && bot2.equals(bot));
    }

    public boolean isBotLoser(Bot bot) {
        return (isBot1Winner() && bot2.equals(bot)) || (isBot2Winner() && bot1.equals(bot));
    }

    public boolean isMatchOfUser(User user) {
        return bot1.getUser().equals(user) || bot2.getUser().equals(user);
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
