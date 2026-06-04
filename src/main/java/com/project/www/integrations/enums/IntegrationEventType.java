package com.project.www.integrations.enums;

public enum IntegrationEventType {
    LEAD_CREATED("lead.created"),
    LEAD_UPDATED("lead.updated"),
    LEAD_STATUS_CHANGED("lead.status.changed"),
    LEAD_FOLLOWUP_SCHEDULED("lead.followup.scheduled"),
    EMPLOYEE_CREATED("employee.created"),
    EMPLOYEE_INVITED("employee.invited"),
    ATTENDANCE_MARKED("attendance.marked"),
    LEAVE_APPROVED("leave.approved"),
    PAYROLL_GENERATED("payroll.generated"),
    PAYMENT_INITIATED("payment.initiated"),
    PAYMENT_SUCCESS("payment.success"),
    PAYMENT_FAILED("payment.failed"),
    SUPPORT_TICKET_CREATED("support.ticket.created"),
    SUPPORT_TICKET_UPDATED("support.ticket.updated"),
    DOCUMENT_GENERATED("document.generated"),
    CERTIFICATE_GENERATED("certificate.generated"),
    MEETING_SCHEDULED("meeting.scheduled");

    private final String eventName;

    IntegrationEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public static IntegrationEventType fromEventName(String eventName) {
        for (IntegrationEventType type : values()) {
            if (type.eventName.equals(eventName)) {
                return type;
            }
        }
        return null;
    }
}
