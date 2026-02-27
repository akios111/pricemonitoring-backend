package iee.ihu.gr.pricemonitoringbackend

class EntityAlreadyExistsException : RuntimeException()

class MonitorListDuplicateNameException(val monitorListName: String) : RuntimeException()

class EmailProviderLimitExceededException : RuntimeException()