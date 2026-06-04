package com.project.www.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import com.project.www.enums.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "default_min_payout_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal defaultMinPayoutAmount;

    @Column(name = "max_payout_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal maxPayoutAmount = BigDecimal.ZERO;

    @Column(name = "student_withdrawal_enabled", nullable = false)
    @Builder.Default
    private boolean studentWithdrawalEnabled = false;

    @Column(name = "max_pending_payouts", nullable = false)
    @Builder.Default
    private int maxPendingPayouts = 1;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
