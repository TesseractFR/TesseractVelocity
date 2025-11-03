import jakarta.persistence.*
import onl.tesseract.tesseractVelocity.domain.vote.VoteSite

@Entity
@Table(name = "t_vote_site")
open class VoteSiteEntity(
    @Id
    @Column(name = "service_name", nullable = false)
    open var serviceName: String,

    @Column(name = "address", nullable = false)
    open var address: String,

    @Column(name = "delay_minutes", nullable = false)
    open var delayMinutes: Int
) {
    fun toDomain(): VoteSite = VoteSite(serviceName, address, delayMinutes)
}

fun VoteSite.toEntity(): VoteSiteEntity {
    return VoteSiteEntity(this.serviceName,this.address,this.delayMinutes)
}
