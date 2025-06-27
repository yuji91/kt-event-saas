import gql from 'graphql-tag';
import * as Urql from 'urql';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type MakeEmpty<T extends { [key: string]: unknown }, K extends keyof T> = { [_ in K]?: never };
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
export type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: { input: string; output: string; }
  String: { input: string; output: string; }
  Boolean: { input: boolean; output: boolean; }
  Int: { input: number; output: number; }
  Float: { input: number; output: number; }
};

export type CustomerInfo = {
  __typename?: 'CustomerInfo';
  email: Scalars['String']['output'];
  role: Scalars['String']['output'];
  tenantId: Scalars['ID']['output'];
};

export type CustomerLoginInput = {
  email: Scalars['String']['input'];
  password: Scalars['String']['input'];
};

export type CustomerLoginPayload = {
  __typename?: 'CustomerLoginPayload';
  accessToken: Scalars['String']['output'];
  expiresIn: Scalars['Int']['output'];
  refreshToken?: Maybe<Scalars['String']['output']>;
  role: Scalars['String']['output'];
  tenantId: Scalars['ID']['output'];
};

export type Mutation = {
  __typename?: 'Mutation';
  loginCustomer: CustomerLoginPayload;
  loginOrganizer: OrganizerLoginPayload;
  refreshCustomerToken: CustomerLoginPayload;
  refreshOrganizerToken: OrganizerLoginPayload;
};


export type MutationLoginCustomerArgs = {
  input: CustomerLoginInput;
};


export type MutationLoginOrganizerArgs = {
  input: OrganizerLoginInput;
};


export type MutationRefreshCustomerTokenArgs = {
  token: Scalars['String']['input'];
};


export type MutationRefreshOrganizerTokenArgs = {
  token: Scalars['String']['input'];
};

export type OrganizerInfo = {
  __typename?: 'OrganizerInfo';
  email: Scalars['String']['output'];
  role: Scalars['String']['output'];
  tenantId: Scalars['ID']['output'];
};

export type OrganizerLoginInput = {
  email: Scalars['String']['input'];
  password: Scalars['String']['input'];
};

export type OrganizerLoginPayload = {
  __typename?: 'OrganizerLoginPayload';
  accessToken: Scalars['String']['output'];
  email: Scalars['String']['output'];
  expiresIn: Scalars['Int']['output'];
  refreshToken?: Maybe<Scalars['String']['output']>;
  role: Scalars['String']['output'];
  tenantId: Scalars['ID']['output'];
};

export type Query = {
  __typename?: 'Query';
  currentCustomer: CustomerInfo;
  currentOrganizer: OrganizerInfo;
};

export type CurrentOrganizerQueryVariables = Exact<{ [key: string]: never; }>;


export type CurrentOrganizerQuery = { __typename?: 'Query', currentOrganizer: { __typename?: 'OrganizerInfo', email: string, role: string, tenantId: string } };

export type LoginOrganizerMutationVariables = Exact<{
  input: OrganizerLoginInput;
}>;


export type LoginOrganizerMutation = { __typename?: 'Mutation', loginOrganizer: { __typename?: 'OrganizerLoginPayload', accessToken: string, refreshToken?: string | null, expiresIn: number, tenantId: string, email: string, role: string } };


export const CurrentOrganizerDocument = gql`
    query CurrentOrganizer {
  currentOrganizer {
    email
    role
    tenantId
  }
}
    `;

export function useCurrentOrganizerQuery(options?: Omit<Urql.UseQueryArgs<CurrentOrganizerQueryVariables>, 'query'>) {
  return Urql.useQuery<CurrentOrganizerQuery, CurrentOrganizerQueryVariables>({ query: CurrentOrganizerDocument, ...options });
};
export const LoginOrganizerDocument = gql`
    mutation LoginOrganizer($input: OrganizerLoginInput!) {
  loginOrganizer(input: $input) {
    accessToken
    refreshToken
    expiresIn
    tenantId
    email
    role
  }
}
    `;

export function useLoginOrganizerMutation() {
  return Urql.useMutation<LoginOrganizerMutation, LoginOrganizerMutationVariables>(LoginOrganizerDocument);
};