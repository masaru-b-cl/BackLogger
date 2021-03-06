package com.nekogata.backlogger.application.service.dashboard

import com.nekogata.backlogger.domain.issue.IssueRepository
import com.nekogata.backlogger.domain.setting.SettingRepository
import com.nekogata.backlogger.infrastructure.api_client.BackLogApiClient
import com.nekogata.backlogger.infrastructure.issue.IssueRepositoryImpl
import com.nekogata.backlogger.infrastructure.setting.SettingRepositoryImpl
import com.nekogata.backlogger.js_exports.events.IssueEvents

import scala.util.{Failure, Success}

class ChangeStatusService {
  val issueRepository: IssueRepository = new IssueRepositoryImpl
  val settingRepository: SettingRepository = new SettingRepositoryImpl

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  def makeStatusAsUntreated(projectId: Int, issueId: Int): Unit = {
    val oldIssue = issueRepository.get(projectId, issueId)
    if ( oldIssue.synchronizing ) {
      return
    }

    val issue = oldIssue.makeStatusUntreatedAndSync()
    issueRepository.store(projectId, issue)
    IssueEvents.repositoryChanged.fire()

    val setting = settingRepository.get()
    val client = new BackLogApiClient(setting.spaceName, setting.apiKey)
    client.changeIssueStatusToUntreated(issueId).onComplete {
      case Success(_) =>
        issueRepository.store(projectId, issue.commitSynchronizing)
        IssueEvents.repositoryChanged.fire()
      case Failure(_) =>
        issueRepository.store(projectId, oldIssue)
        IssueEvents.repositoryChanged.fire()
    }
  }

  def makeStatusAsProcessing(projectId: Int, issueId: Int): Unit = {
    val oldIssue = issueRepository.get(projectId, issueId)
    if ( oldIssue.synchronizing ) {
      return
    }

    val issue = oldIssue.makeStatusProcessingAndSync()
    issueRepository.store(projectId, issue)
    IssueEvents.repositoryChanged.fire()

    val setting = settingRepository.get()
    val client = new BackLogApiClient(setting.spaceName, setting.apiKey)
    client.changeIssueStatusToProcessing(issueId).onComplete {
      case Success(_) =>
        issueRepository.store(projectId, issue.commitSynchronizing)
        IssueEvents.repositoryChanged.fire()
      case Failure(_) =>
        issueRepository.store(projectId, oldIssue)
        IssueEvents.repositoryChanged.fire()
    }
  }

  def makeStatusAsProcessed(projectId: Int, issueId: Int): Unit = {
    val oldIssue = issueRepository.get(projectId, issueId)
    if ( oldIssue.synchronizing ) {
      return
    }

    val issue = oldIssue.makeStatusProcessedAndSync()
    issueRepository.store(projectId, issue)
    IssueEvents.repositoryChanged.fire()

    val setting = settingRepository.get()
    val client = new BackLogApiClient(setting.spaceName, setting.apiKey)
    client.changeIssueStatusToProcessed(issueId).onComplete {
      case Success(_) =>
        issueRepository.store(projectId, issue.commitSynchronizing)
        IssueEvents.repositoryChanged.fire()
      case Failure(_) =>
        issueRepository.store(projectId, oldIssue)
        IssueEvents.repositoryChanged.fire()
    }
  }


}
