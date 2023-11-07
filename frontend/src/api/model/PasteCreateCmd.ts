
export type PasteCreateCmd = {
  title?: string
  content: string
  isEncrypted?: boolean
  expiry?: 'ONE_HOUR' | 'ONE_DAY' | 'ONE_WEEK' | 'ONE_MONTH' | 'THREE_MONTHS' | 'ONE_YEAR' | 'NEVER'
  exposure?: 'PUBLIC' | 'UNLISTED' | 'ONCE'
}
