package com.swisscom.cloud.sb.broker.backup.converter

import com.swisscom.cloud.sb.broker.backup.RestoreStatusConverter
import com.swisscom.cloud.sb.broker.model.Backup
import com.swisscom.cloud.sb.broker.model.Restore
import com.swisscom.cloud.sb.model.backup.RestoreStatus
import spock.lang.Specification

class RestoreDtoConverterSpec extends Specification {
    private RestoreDtoConverter restoreDtoConverter

    def setup() {
        restoreDtoConverter = new RestoreDtoConverter(new RestoreStatusConverter())
    }

    def "conversion happy path"() {
        given:
        def source = new Restore()
        source.guid = "someId"
        source.backup = new Backup(guid: "backupId")
        source.status = dbStatus
        when:
        def result = restoreDtoConverter.convert(source)
        then:
        source.guid == result.id
        source.backup.guid == result.backup_id
        source.dateRequested == result.created_at
        source.dateUpdated == result.updated_at
        dtoStatus == result.status
        where:
        dbStatus           | dtoStatus
        Backup.Status.INIT | RestoreStatus.IN_PROGRESS

    }
}
